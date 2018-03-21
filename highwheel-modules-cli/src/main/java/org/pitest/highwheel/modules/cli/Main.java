package org.pitest.highwheel.modules.cli;

import org.pitest.highwheel.bytecodeparser.ClassPathParser;
import org.pitest.highwheel.bytecodeparser.classpath.ArchiveClassPathRoot;
import org.pitest.highwheel.bytecodeparser.classpath.CompoundClassPathRoot;
import org.pitest.highwheel.bytecodeparser.classpath.DirectoryClassPathRoot;
import org.pitest.highwheel.classpath.ClassParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.modules.AnalyserModel;
import org.pitest.highwheel.modules.ModuleAnalyser;
import org.pitest.highwheel.modules.model.Definition;
import org.pitest.highwheel.modules.specification.Compiler;
import org.pitest.highwheel.modules.specification.SyntaxTree;
import org.pitest.highwheel.modules.specification.parsers.DefinitionParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.pitest.highwheel.util.StringUtil.join;

public class Main {

    private static Filter includeAll = new Filter() {
        @Override
        public boolean include(ElementName item) {
            return true;
        }
    };

    public static void main(String[] argv) {
        try {
            final CmdParser cmdParser = new CmdParser(argv);
            final ClasspathRoot rootClassPath = getAnalysisScope(cmdParser.argList);

            System.out.println("- Compiling specification...");
            final SyntaxTree.Definition syntaxDefinition = getDefinition(cmdParser.specificationFile);
            final Definition definition = compileDefinition(syntaxDefinition);
            System.out.println("- Done!");

            final ClassParser classParser = new ClassPathParser(includeAll);
            final ModuleAnalyser analyser = new ModuleAnalyser(classParser);
            if (cmdParser.mode == ExecutionMode.STRICT) {
                strictAnalysis(analyser,definition,rootClassPath);
            } else {
                looseAnalysis(analyser,definition,rootClassPath);
            }
        }catch(Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static ClasspathRoot getAnalysisScope(List<String> paths) {
        final List<File> jars = new ArrayList<File>();
        final List<File> dirs = new ArrayList<File>();
        final List<String> ignored = new ArrayList<String>();
        for(String path : paths) {
            final File f = new File(path);
            if(!f.exists() || ! f.canRead() || (f.isFile() && !path.endsWith(".jar"))) {
                ignored.add(path);
            } else if(f.isDirectory()) {
                dirs.add(f);
            } else {
                jars.add(f);
            }
        }
        System.out.println("- Ignoring: " + join(", ", ignored));
        System.out.println("- Directories: "+ join(", ", getPaths(dirs)));
        System.out.println("- Jars:" + join(", ", getPaths(jars)));

        final List<ClasspathRoot> classpathRoots = new ArrayList<ClasspathRoot>();
        for(File jar : jars) {
            classpathRoots.add(new ArchiveClassPathRoot(jar));
        }
        for(File dir : dirs) {
            classpathRoots.add(new DirectoryClassPathRoot(dir));
        }

        return new CompoundClassPathRoot(classpathRoots);
    }

    private static void strictAnalysis(ModuleAnalyser analyser, Definition definition, ClasspathRoot classpathRoot) {
        AnalyserModel.StrictAnalysisResult analysisResult = analyser.analyseStrict(classpathRoot, definition);
        System.out.println("- Analysis complete");
        System.out.println("- Metrics: ");
        printMetrics(analysisResult.metrics);
        boolean error = !analysisResult.dependencyViolations.isEmpty() || !analysisResult.noStrictDependencyViolations.isEmpty();
        if(analysisResult.dependencyViolations.isEmpty()) {
            System.out.println("- No dependency violation detected");
        } else {
            System.err.println("- The following dependencies violate the specification:");
            printDependencyViolations(analysisResult.dependencyViolations);
        }
        if(analysisResult.noStrictDependencyViolations.isEmpty()) {
            System.out.println("- No direct dependency violation detected");
        } else {
            System.err.println("- The following direct dependencies violate the specification:");
            printNoDirectDependecyViolation(analysisResult.noStrictDependencyViolations);
        }
        if(error){
            throw new CliException("");
        }
    }

    private static void looseAnalysis(ModuleAnalyser analyser, Definition definition, ClasspathRoot classpathRoot){
        AnalyserModel.LooseAnalysisResult analysisResult = analyser.analyseLoose(classpathRoot, definition);
        System.out.println("- Analysis complete");
        System.out.println("- Metrics: ");
        printMetrics(analysisResult.metrics);
        boolean error = !analysisResult.absentDependencyViolations.isEmpty() || !analysisResult.undesiredDependencyViolations.isEmpty();
        if(analysisResult.absentDependencyViolations.isEmpty()) {
            System.out.println("- All dependencies specified exist");
        } else {
            System.err.println("- The following dependencies do not exist:");
            printAbsentDependencies(analysisResult.absentDependencyViolations);
        }
        if(analysisResult.undesiredDependencyViolations.isEmpty()) {
            System.out.println("- No dependency violation detected");
        } else {
            System.err.println("- The following dependencies violate the specification:");
            printUndesiredDependencies(analysisResult.undesiredDependencyViolations);
        }
        if(error){
            throw new CliException("");
        }
    }

    private static void printMetrics(Collection<AnalyserModel.Metrics> metrics) {
        for(AnalyserModel.Metrics m : metrics) {
            System.err.println(String.format("  - %20s --> fanIn: %5d, fanOut: %5d", m.module, m.fanIn,m.fanOut));
        }
    }

    private static void printDependencyViolations(Collection<AnalyserModel.DependencyViolation> violations) {
        for(AnalyserModel.DependencyViolation violation: violations) {
            System.err.println(String.format("  - %s -> %s. Expected path: %s, Actual path: %s",
                    violation.sourceModule,
                    violation.destinationModule,
                violation.specificationPath.isEmpty() ? "(empty)" : violation.sourceModule + " -> " + join(" -> ", violation.specificationPath),
                violation.actualPath.isEmpty() ? "(empty)" : violation.sourceModule + " -> " + join(" -> ", violation.actualPath)));
        }
    }

    private static void printNoDirectDependecyViolation(Collection<AnalyserModel.NoStrictDependencyViolation> violations) {
        for(AnalyserModel.NoStrictDependencyViolation violation: violations) {
            System.err.println(String.format("  - %s -> %s",
                    violation.sourceModule,
                    violation.destinationModule));
        }
    }

    private static void printAbsentDependencies(Collection<AnalyserModel.AbsentDependencyViolation> violations) {
        for(AnalyserModel.AbsentDependencyViolation violation: violations) {
            System.err.println(String.format("  - %s -> %s",violation.sourceModule,violation.destinationModule));
        }
    }

    private static void printUndesiredDependencies(Collection<AnalyserModel.UndesiredDependencyViolation> violations) {
        for(AnalyserModel.UndesiredDependencyViolation violation: violations) {
            System.err.println(String.format("  - %s -/-> %s. Actual dependency path: %s",
                    violation.sourceModule,
                    violation.destinationModule,
                violation.evidence.isEmpty() ? "(empty)" : violation.sourceModule + " -> " + join(" -> ", violation.evidence)));
        }
    }

    private static SyntaxTree.Definition getDefinition(File specificationFile) {
        final DefinitionParser definitionParser = new DefinitionParser();
        try {
            return definitionParser.parse(new FileReader(specificationFile));
        } catch(IOException e) {
            throw new CliException("Error while parsing the specification file: "+ e.getMessage());
        }
    }

    private static Definition compileDefinition(SyntaxTree.Definition definition) {
        final Compiler compiler = new Compiler();
        try {
            return compiler.compile(definition);
        } catch(RuntimeException e) {
            System.err.println("Error while compiling the specification file: "+ e.getMessage());
            System.exit(1);
            return null;
        }
    }

    private static List<String> getPaths(List<File> files) {
        final List<String> result = new ArrayList<String>();
        for(File f: files) {
            result.add(f.getAbsolutePath());
        }
        return result;
    }
}

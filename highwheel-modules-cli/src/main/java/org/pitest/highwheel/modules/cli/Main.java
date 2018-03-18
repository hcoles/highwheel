package org.pitest.highwheel.modules.cli;

import org.apache.commons.cli.*;
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

    private static String STRICT = "strict";
    private static String LOOSE = "loose";

    private static Filter includeAll = new Filter() {
        @Override
        public boolean include(ElementName item) {
            return true;
        }
    };

    public static void main(String[] argv) {
        final Options options = new Options();
        final Option spec = Option.builder("s")
                .longOpt("specification")
                .optionalArg(false)
                .hasArg(true)
                .desc("Path to the specification file").build();
        final Option mode = Option.builder("m")
                .longOpt("mode")
                .optionalArg(false)
                .hasArg(true)
                .desc("Mode of analysis. Can be 'strict' or 'loose'").build();
        options.addOption(spec).addOption(mode);

        final CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, argv);
        } catch(ParseException e) {
            System.err.println("Error while parsing the command line arguments: "+ e.getMessage());
            System.exit(1);
        }
        String specificationPath = cmd.getOptionValue("specification","spec.hwm");
        String operationMode = cmd.getOptionValue("mode",STRICT);
        if(! operationMode.equals(STRICT) && !operationMode.equals(LOOSE)) {
            System.err.println("Unrecognised value for mode: " + operationMode + ". Select 'strict' or 'loose'");
            System.exit(1);
        }

        final File specificationFile = new File(specificationPath);
        if(!specificationFile.exists() || specificationFile.isDirectory() || !specificationFile.canRead()) {
            System.err.println(String.format("Cannot read from specification file '%s'.", specificationPath));
            System.exit(1);
        }

        final List<File> jars = new ArrayList<File>();
        final List<File> dirs = new ArrayList<File>();
        final List<String> ignored = new ArrayList<String>();
        for(String path : cmd.getArgList()) {
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
        System.out.println("- Compiling specification...");

        final SyntaxTree.Definition syntaxDefinition = getDefinition(specificationFile);
        final Definition definition = compileDefinition(syntaxDefinition);

        System.out.println("- Done!");

        final List<ClasspathRoot> classpathRoots = new ArrayList<ClasspathRoot>();
        for(File jar : jars) {
            classpathRoots.add(new ArchiveClassPathRoot(jar));
        }
        for(File dir : dirs) {
            classpathRoots.add(new DirectoryClassPathRoot(dir));
        }

        final ClasspathRoot rootClassPath = new CompoundClassPathRoot(classpathRoots);
        final ClassParser classParser = new ClassPathParser(includeAll);
        final ModuleAnalyser analyser = new ModuleAnalyser(classParser);
        try {
            if (operationMode.equals(STRICT)) {
                AnalyserModel.StrictAnalysisResult analysisResult = analyser.analyseStrict(rootClassPath, definition);
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
                    System.exit(1);
                }
            } else {
                AnalyserModel.LooseAnalysisResult analysisResult = analyser.analyseLoose(rootClassPath, definition);
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
                    System.exit(1);
                }
            }
        } catch(Exception e) {
            System.err.println("Error during analysis: "+ e.getMessage());
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
                    violation.sourceModule + " -> " + join(" -> ", violation.specificationPath),
                    violation.sourceModule + " -> " + join(" -> ", violation.actualPath)));
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
                    violation.sourceModule + " -> " + join(" -> ", violation.evidence)));
        }
    }

    private static SyntaxTree.Definition getDefinition(File specificationFile) {
        final DefinitionParser definitionParser = new DefinitionParser();
        try {
            return definitionParser.parse(new FileReader(specificationFile));
        } catch(IOException e) {
            System.err.println("Error while parsing the specification file: "+ e.getMessage());
            System.exit(1);
            return null;
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

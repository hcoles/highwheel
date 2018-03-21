package org.pitest.highwheel.modules;

import org.pitest.highwheel.bytecodeparser.ClassPathParser;
import org.pitest.highwheel.bytecodeparser.classpath.ArchiveClassPathRoot;
import org.pitest.highwheel.bytecodeparser.classpath.CompoundClassPathRoot;
import org.pitest.highwheel.bytecodeparser.classpath.DirectoryClassPathRoot;
import org.pitest.highwheel.classpath.ClassParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
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

public class AnalyserFacade {

  private static Filter includeAll = (item) -> true;

  public enum ExecutionMode {
    STRICT, LOOSE
  }

  public interface Printer {
    void info(String msg, int indentation);
    void info(String msg);
    void error(String msg, int indentation);
    void error(String msg);
  }

  private final Printer printer;

  public AnalyserFacade(Printer printer) {
    this.printer = printer;
  }


  public void runAnalysis(final List<String> classPathRoots, final String specificationPath, final ExecutionMode executionMode) {
    final ClasspathRoot classpathRoot = getAnalysisScope(classPathRoots);
    final File specificationFile = new File(specificationPath);
    if(!specificationFile.exists() || specificationFile.isDirectory() || !specificationFile.canRead()) {
      throw new AnalyserException(String.format("Cannot read from specification file '%s'.", specificationPath));
    }
    printer.info("Compiling specification...");
    final SyntaxTree.Definition syntaxDefinition = getDefinition(specificationFile);
    final Definition definition = compileDefinition(syntaxDefinition);
    printer.info("Done!");
    final ClassParser classParser = new ClassPathParser(includeAll);
    final ModuleAnalyser analyser = new ModuleAnalyser(classParser);
    if (executionMode == ExecutionMode.STRICT) {
      strictAnalysis(analyser,definition,classpathRoot);
    } else {
      looseAnalysis(analyser,definition,classpathRoot);
    }
    
  }

  public void runAnalysis(final List<String> classPathRoots, final String specificationPath) {
    runAnalysis(classPathRoots,specificationPath,ExecutionMode.STRICT);
  }

  private ClasspathRoot getAnalysisScope(List<String> paths) {
    final List<File> jars = new ArrayList<>();
    final List<File> dirs = new ArrayList<>();
    final List<String> ignored = new ArrayList<>();
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
    printer.info("Ignoring: " + join(", ", ignored));
    printer.info("Directories: "+ join(", ", getPaths(dirs)));
    printer.info("Jars:" + join(", ", getPaths(jars)));

    final List<ClasspathRoot> classpathRoots = new ArrayList<ClasspathRoot>();
    for(File jar : jars) {
      classpathRoots.add(new ArchiveClassPathRoot(jar));
    }
    for(File dir : dirs) {
      classpathRoots.add(new DirectoryClassPathRoot(dir));
    }

    return new CompoundClassPathRoot(classpathRoots);
  }

  private List<String> getPaths(List<File> files) {
    final List<String> result = new ArrayList<String>();
    for(File f: files) {
      result.add(f.getAbsolutePath());
    }
    return result;
  }

  private SyntaxTree.Definition getDefinition(File specificationFile) {
    final DefinitionParser definitionParser = new DefinitionParser();
    try {
      return definitionParser.parse(new FileReader(specificationFile));
    } catch(IOException e) {
      throw new AnalyserException("Error while parsing the specification file: "+ e.getMessage());
    }
  }

  private static Definition compileDefinition(SyntaxTree.Definition definition) {
    final Compiler compiler = new Compiler();
    return compiler.compile(definition);
  }

  private void strictAnalysis(ModuleAnalyser analyser, Definition definition, ClasspathRoot classpathRoot) {
    AnalyserModel.StrictAnalysisResult analysisResult = analyser.analyseStrict(classpathRoot, definition);
    printer.info("Analysis complete");
    printer.info("Metrics: ");
    printMetrics(analysisResult.metrics);
    boolean error = !analysisResult.dependencyViolations.isEmpty() || !analysisResult.noStrictDependencyViolations.isEmpty();
    if(analysisResult.dependencyViolations.isEmpty()) {
      printer.info("No dependency violation detected");
    } else {
      printer.error("The following dependencies violate the specification:");
      printDependencyViolations(analysisResult.dependencyViolations);
    }
    if(analysisResult.noStrictDependencyViolations.isEmpty()) {
      printer.info("No direct dependency violation detected");
    } else {
      printer.error("The following direct dependencies violate the specification:");
      printNoDirectDependecyViolation(analysisResult.noStrictDependencyViolations);
    }
    if(error){
      throw new AnalyserException("Analysis failed");
    }
  }

  private void looseAnalysis(ModuleAnalyser analyser, Definition definition, ClasspathRoot classpathRoot){
    AnalyserModel.LooseAnalysisResult analysisResult = analyser.analyseLoose(classpathRoot, definition);
    printer.info("Analysis complete");
    printer.info("Metrics: ");
    printMetrics(analysisResult.metrics);
    boolean error = !analysisResult.absentDependencyViolations.isEmpty() || !analysisResult.undesiredDependencyViolations.isEmpty();
    if(analysisResult.absentDependencyViolations.isEmpty()) {
      printer.info("All dependencies specified exist");
    } else {
      printer.error("The following dependencies do not exist:");
      printAbsentDependencies(analysisResult.absentDependencyViolations);
    }
    if(analysisResult.undesiredDependencyViolations.isEmpty()) {
      printer.info("No dependency violation detected");
    } else {
      printer.error("The following dependencies violate the specification:");
      printUndesiredDependencies(analysisResult.undesiredDependencyViolations);
    }
    if(error){
      throw new AnalyserException("Analysis failed");
    }
  }

  private void printMetrics(Collection<AnalyserModel.Metrics> metrics) {
    for(AnalyserModel.Metrics m : metrics) {
      printer.info(String.format("%20s --> fanIn: %5d, fanOut: %5d", m.module, m.fanIn,m.fanOut),1);
    }
  }

  private void printDependencyViolations(Collection<AnalyserModel.DependencyViolation> violations) {
    for(AnalyserModel.DependencyViolation violation: violations) {
      printer.error(String.format("%s -> %s. Expected path: %s, Actual path: %s",
          violation.sourceModule,
          violation.destinationModule,
          violation.specificationPath.isEmpty() ? "(empty)" : violation.sourceModule + " -> " + join(" -> ", violation.specificationPath),
          violation.actualPath.isEmpty() ? "(empty)" : violation.sourceModule + " -> " + join(" -> ", violation.actualPath)),1);
    }
  }

  private void printNoDirectDependecyViolation(Collection<AnalyserModel.NoStrictDependencyViolation> violations) {
    for(AnalyserModel.NoStrictDependencyViolation violation: violations) {
      printer.error(String.format("%s -> %s",
          violation.sourceModule,
          violation.destinationModule),1);
    }
  }

  private void printAbsentDependencies(Collection<AnalyserModel.AbsentDependencyViolation> violations) {
    for(AnalyserModel.AbsentDependencyViolation violation: violations) {
      printer.error(String.format("%s -> %s",violation.sourceModule,violation.destinationModule),1);
    }
  }

  private void printUndesiredDependencies(Collection<AnalyserModel.UndesiredDependencyViolation> violations) {
    for(AnalyserModel.UndesiredDependencyViolation violation: violations) {
      printer.error(String.format("%s -/-> %s. Actual dependency path: %s",
          violation.sourceModule,
          violation.destinationModule,
          violation.evidence.isEmpty() ? "(empty)" : violation.sourceModule + " -> " + join(" -> ", violation.evidence)),1);
    }
  }

  
}

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
    void info(String msg);
  }

  public interface EventSink {
    interface PathEventSink {
      void ignoredPaths(List<String> ignored);

      void directories(List<String> directories);

      void jars(List<String> jars);
    }

    interface MeasureEventSink {
      void fanInOutMeasure(String module, int fanIn, int fanOut);
    }

    interface StrictAnalysisEventSink {
      void dependenciesCorrect();

      void directDependenciesCorrect();

      void dependencyViolationsPresent();

      void dependencyViolation(String sourceModel, String destModel, List<String> expectedPath, List<String> actualPath);

      void noDirectDependenciesViolationPresent();

      void noDirectDependencyViolation(String sourceModule, String destModule);
    }

    interface LooseAnalysisEventSink {

      void allDependenciesPresent();

      void noUndesiredDependencies();

      void absentDependencyViolationsPresent();

      void absentDependencyViolation(String sourceModule, String destModule);

      void undesiredDependencyViolationsPresent();

      void undesiredDependencyViolation(String sourceModule, String destModule, List<String> path);
    }
  }

  private final Printer printer;
  private final EventSink.PathEventSink pathEventSink;
  private final EventSink.MeasureEventSink measureEventSink;
  private final EventSink.StrictAnalysisEventSink strictAnalysisEventSink;
  private final EventSink.LooseAnalysisEventSink looseAnalysisEventSink;

  public AnalyserFacade(final Printer printer,
      final EventSink.PathEventSink pathEventSink,
      final EventSink.MeasureEventSink measureEventSink,
      final EventSink.StrictAnalysisEventSink strictAnalysisEventSink,
      final EventSink.LooseAnalysisEventSink looseAnalysisEventSink) {
    this.printer = printer;
    this.pathEventSink = pathEventSink;
    this.measureEventSink = measureEventSink;
    this.strictAnalysisEventSink = strictAnalysisEventSink;
    this.looseAnalysisEventSink = looseAnalysisEventSink;
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
    pathEventSink.ignoredPaths(ignored);
    pathEventSink.directories(getPaths(dirs));
    pathEventSink.jars(getPaths(jars));

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
    printMetrics(analysisResult.metrics);
    boolean error = !analysisResult.dependencyViolations.isEmpty() || !analysisResult.noStrictDependencyViolations.isEmpty();
    if(analysisResult.dependencyViolations.isEmpty()) {
      strictAnalysisEventSink.dependenciesCorrect();
    } else {
      strictAnalysisEventSink.dependencyViolationsPresent();
      printDependencyViolations(analysisResult.dependencyViolations);
    }
    if(analysisResult.noStrictDependencyViolations.isEmpty()) {
      strictAnalysisEventSink.directDependenciesCorrect();
    } else {
      strictAnalysisEventSink.noDirectDependenciesViolationPresent();
      printNoDirectDependecyViolation(analysisResult.noStrictDependencyViolations);
    }
    if(error){
      throw new AnalyserException("Analysis failed");
    }
  }

  private void looseAnalysis(ModuleAnalyser analyser, Definition definition, ClasspathRoot classpathRoot){
    AnalyserModel.LooseAnalysisResult analysisResult = analyser.analyseLoose(classpathRoot, definition);
    printer.info("Analysis complete");
    printMetrics(analysisResult.metrics);
    boolean error = !analysisResult.absentDependencyViolations.isEmpty() || !analysisResult.undesiredDependencyViolations.isEmpty();
    if(analysisResult.absentDependencyViolations.isEmpty()) {
      looseAnalysisEventSink.allDependenciesPresent();
    } else {
      looseAnalysisEventSink.absentDependencyViolationsPresent();
      printAbsentDependencies(analysisResult.absentDependencyViolations);
    }
    if(analysisResult.undesiredDependencyViolations.isEmpty()) {
      looseAnalysisEventSink.noUndesiredDependencies();
    } else {
      looseAnalysisEventSink.undesiredDependencyViolationsPresent();
      printUndesiredDependencies(analysisResult.undesiredDependencyViolations);
    }
    if(error){
      throw new AnalyserException("Analysis failed");
    }
  }

  private void printMetrics(Collection<AnalyserModel.Metrics> metrics) {
    for(AnalyserModel.Metrics m : metrics) {
      measureEventSink.fanInOutMeasure(m.module,m.fanIn,m.fanOut);
    }
  }

  private void printDependencyViolations(Collection<AnalyserModel.DependencyViolation> violations) {
    for(AnalyserModel.DependencyViolation violation: violations) {
      strictAnalysisEventSink.dependencyViolation(violation.sourceModule,violation.destinationModule,
          appendStartIfNotEmpty(violation.specificationPath,violation.sourceModule),
          appendStartIfNotEmpty(violation.actualPath,violation.sourceModule));
    }
  }

  private <T> List<T> appendStartIfNotEmpty(List<T> collection, T element) {
    if(collection.isEmpty()) {
      return collection;
    } else {
      final List<T> result = new ArrayList<>(collection.size() + 1);
      result.add(element);
      result.addAll(collection);
      return result;
    }
  }
  private void printNoDirectDependecyViolation(Collection<AnalyserModel.NoStrictDependencyViolation> violations) {
    for(AnalyserModel.NoStrictDependencyViolation violation: violations) {
      strictAnalysisEventSink.noDirectDependencyViolation(violation.sourceModule, violation.destinationModule);
    }
  }

  private void printAbsentDependencies(Collection<AnalyserModel.AbsentDependencyViolation> violations) {
    for(AnalyserModel.AbsentDependencyViolation violation: violations) {
      looseAnalysisEventSink.absentDependencyViolation(violation.sourceModule,violation.destinationModule);
    }
  }

  private void printUndesiredDependencies(Collection<AnalyserModel.UndesiredDependencyViolation> violations) {
    for(AnalyserModel.UndesiredDependencyViolation violation: violations) {
      looseAnalysisEventSink.undesiredDependencyViolation(
          violation.sourceModule,
          violation.destinationModule,
          appendStartIfNotEmpty(violation.evidence,violation.sourceModule));
    }
  }

  
}

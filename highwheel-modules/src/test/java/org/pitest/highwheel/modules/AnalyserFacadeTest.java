package org.pitest.highwheel.modules;

import org.jparsec.error.ParserException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.pitest.highwheel.modules.specification.CompilerException;

import static org.pitest.highwheel.util.StringUtil.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.mockito.MockitoAnnotations.initMocks;

public class AnalyserFacadeTest {
  @Mock
  private AnalyserFacade.Printer printer;

  @Mock
  private AnalyserFacade.EventSink.PathEventSink pathEventSink;

  @Mock
  private AnalyserFacade.EventSink.MeasureEventSink measureEventSink;

  @Mock
  private AnalyserFacade.EventSink.StrictAnalysisEventSink strictAnalysisEventSink;

  @Mock
  private AnalyserFacade.EventSink.LooseAnalysisEventSink looseAnalysisEventSink;

  private AnalyserFacade testee;

  private final String defaultSpec = join(File.separator,Arrays.asList("src","test","resources","spec.hwm"));
  private final String jarPath = join(File.separator,Arrays.asList("src","test","resources","highwheel-model.jar"));
  private final String wrongSpec = join(File.separator,Arrays.asList("src","test","resources", "wrong-syntax-spec.hwm"));
  private final String wrongSemanticsSpec = join(File.separator,Arrays.asList("src","test","resources", "wrong-semantics-spec.hwm"));
  private final String wrongStrictDefinitionSpec = join(File.separator,Arrays.asList("src","test","resources", "wrong-strict-spec.hwm"));
  private final String looseSpec = join(File.separator,Arrays.asList("src","test","resources", "loose-spec.hwm"));
  private final String orgExamplePath = join(File.separator,Arrays.asList("target","test-classes","org"));
  private final String wrongLooseDefinitionSpec = join(File.separator,Arrays.asList("src","test","resources", "wrong-loose-spec.hwm"));


  @Before
  public void setUp() {
    initMocks(this);
    testee = new AnalyserFacade(printer,pathEventSink,measureEventSink,strictAnalysisEventSink,looseAnalysisEventSink);
  }

  private static class CollectionContains extends ArgumentMatcher<List<String>> {

    private final String regex;
    public CollectionContains(String regex) {
      this.regex = regex;
    }
    @Override
    @SuppressWarnings("unchecked")
    public boolean matches(Object o) {
      Collection<String> collection = (Collection) o;
      return collection.stream().anyMatch((el) -> el.matches(regex));
    }
  }

  private static  CollectionContains anyMatches(String regex) {
    return new CollectionContains(regex);
  }

  @Test(expected = AnalyserException.class)
  public void shouldPrintAsInfoJarsThatArePassedAsArgument() {
    try {
      testee.runAnalysis(Arrays.asList(jarPath), defaultSpec);
    } finally {
      verify(pathEventSink).jars(argThat(anyMatches(".*highwheel-model\\.jar.*")));
    }
  }

  @Test
  public void shouldPrintAsInfoDirectoriesThatPassedAsArgument() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),defaultSpec);
    verify(pathEventSink).directories(argThat(anyMatches(".*test-classes.*org.*")));
  }

  @Test(expected = AnalyserException.class)
  public void shouldPrintAsIgnoredFileThatDoNotExist() {
    try {
      testee.runAnalysis(Arrays.asList("foobar"), defaultSpec);
    } finally {
      verify(pathEventSink).ignoredPaths(argThat(anyMatches(".*foobar.*")));
    }
  }

  @Test
  public void shouldPrintAsInfoJarsDiresAndIgnored() {
    testee.runAnalysis(Arrays.asList(jarPath,orgExamplePath,"foobar"),defaultSpec);
    verify(pathEventSink).jars(argThat(anyMatches(".*highwheel-model\\.jar.*")));
    verify(pathEventSink).directories(argThat(anyMatches(".*test-classes.*org.*")));
    verify(pathEventSink).ignoredPaths(argThat(anyMatches(".*foobar.*")));
  }

  @Test(expected = AnalyserException.class)
  public void shoulFailIfSpecificationFileDoesNotExist() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),"foobar");
  }

  @Test(expected = ParserException.class)
  public void shouldFailIfParsingFails() {
    try {
      testee.runAnalysis(Arrays.asList(orgExamplePath), wrongSpec);
    } finally {
      verify(printer).info(matches(".*Compiling specification.*"));
    }
  }

  @Test(expected = CompilerException.class)
  public void shouldFailIfCompilationFails() {
    try {
      testee.runAnalysis(Arrays.asList(orgExamplePath), wrongSemanticsSpec);
    } finally {
      verify(printer).info(matches(".*Compiling specification.*"));
    }
  }

  @Test
  public void strictAnalysisShouldProduceTheExpectedOutputWhenThereAreNoViolation() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),defaultSpec);
    verify(strictAnalysisEventSink).dependenciesCorrect();
    verify(strictAnalysisEventSink).directDependenciesCorrect();
  }

  @Test
  public void looseAnalysisShouldProduceTheExpectedOutputWhenThereAreNoViolation() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),looseSpec, AnalyserFacade.ExecutionMode.LOOSE);
    verify(looseAnalysisEventSink).allDependenciesPresent();
    verify(looseAnalysisEventSink).noUndesiredDependencies();
  }

  @Test
  public void strictAnalysisShouldProduceMetrics() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),defaultSpec);
    verifyStrictMetrics();
  }

  private void verifyStrictMetrics() {
    verify(measureEventSink).fanInOutMeasure("Facade",2,3);
    verify(measureEventSink).fanInOutMeasure("Utils",2,0);
    verify(measureEventSink).fanInOutMeasure("IO",1,3);
    verify(measureEventSink).fanInOutMeasure("Model",4,0);
    verify(measureEventSink).fanInOutMeasure("CoreInternals",1,3);
    verify(measureEventSink).fanInOutMeasure("CoreApi",4,1);
    verify(measureEventSink).fanInOutMeasure("Controller",1,1);
    verify(measureEventSink).fanInOutMeasure("Main",0,4);
  }

  @Test
  public void looseAnalysisShouldProduceMetrics() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),looseSpec,AnalyserFacade.ExecutionMode.LOOSE);
    verifyLooseMetrics();
  }

  private void verifyLooseMetrics() {
    verify(measureEventSink).fanInOutMeasure("Facade",2,3);
    verify(measureEventSink).fanInOutMeasure("Utils",2,0);
    verify(measureEventSink).fanInOutMeasure("IO",1,3);
    verify(measureEventSink).fanInOutMeasure("Model",4,0);
    verify(measureEventSink).fanInOutMeasure("CoreInternals",1,2);
    verify(measureEventSink).fanInOutMeasure("CoreApi",3,1);
    verify(measureEventSink).fanInOutMeasure("Controller",1,1);
    verify(measureEventSink).fanInOutMeasure("Main",0,4);
  }

  @Test(expected = AnalyserException.class)
  public void strictAnalysisShouldFailAndPrintTheViolations() {
    try {
      testee.runAnalysis(Arrays.asList(orgExamplePath),wrongStrictDefinitionSpec);
    } finally {
      verify(strictAnalysisEventSink).dependencyViolationsPresent();
      verify(strictAnalysisEventSink).dependencyViolation("IO","Utils", Collections.emptyList(),Arrays.asList("IO","Utils"));
      verify(strictAnalysisEventSink).noDirectDependenciesViolationPresent();
      verify(strictAnalysisEventSink).noDirectDependencyViolation("Facade","CoreInternals");
    }
  }

  @Test(expected = AnalyserException.class)
  public void looseAnalysisShouldFailAndPrintTheViolations() {
    try {
      testee.runAnalysis(Arrays.asList(orgExamplePath),wrongLooseDefinitionSpec,AnalyserFacade.ExecutionMode.LOOSE);
    } finally {
      verify(looseAnalysisEventSink).absentDependencyViolationsPresent();
      verify(looseAnalysisEventSink).undesiredDependencyViolationsPresent();
      verify(looseAnalysisEventSink).absentDependencyViolation("IO","CoreInternals");
      verify(looseAnalysisEventSink).undesiredDependencyViolation("IO","Model",Arrays.asList("IO","Model"));
    }
  }

}

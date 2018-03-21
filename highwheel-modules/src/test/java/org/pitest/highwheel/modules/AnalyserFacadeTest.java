package org.pitest.highwheel.modules;

import org.jparsec.error.ParserException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.pitest.highwheel.modules.specification.CompilerException;

import static org.pitest.highwheel.util.StringUtil.*;

import java.io.File;
import java.util.Arrays;

import static org.mockito.Mockito.*;

import static org.mockito.MockitoAnnotations.initMocks;

public class AnalyserFacadeTest {
  @Mock
  private AnalyserFacade.Printer printer;

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
    testee = new AnalyserFacade(printer);
  }

  @Test(expected = AnalyserException.class)
  public void shouldPrintAsInfoJarsThatArePassedAsArgument() {
    try {
      testee.runAnalysis(Arrays.asList(jarPath), defaultSpec);
    } finally {
      verify(printer).info(matches(".*Jars:.*highwheel-model\\.jar.*"));
    }
  }

  @Test
  public void shouldPrintAsInfoDirectoriesThatPassedAsArgument() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),defaultSpec);
    verify(printer).info(matches(".*Directories:.*test-classes.*org.*"));
  }

  @Test(expected = AnalyserException.class)
  public void shouldPrintAsIgnoredFileThatDoNotExist() {
    try {
      testee.runAnalysis(Arrays.asList("foobar"), defaultSpec);
    } finally {
      verify(printer).warning(matches(".*Ignoring:.*foobar.*"));
    }
  }

  @Test
  public void shouldPrintAsInfoJarsDiresAndIgnored() {
    testee.runAnalysis(Arrays.asList(jarPath,orgExamplePath,"foobar"),defaultSpec);
    verify(printer).info(matches(".*Jars:.*highwheel-model.*"));
    verify(printer).info(matches(".*Directories:.*test-classes.*org.*"));
    verify(printer).warning(matches(".*Ignoring:.*foobar.*"));
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
    verify(printer).info(matches(".*No dependency violation detected.*"));
    verify(printer).info(matches(".*No direct dependency violation detected.*"));
  }

  @Test
  public void looseAnalysisShouldProduceTheExpectedOutputWhenThereAreNoViolation() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),looseSpec, AnalyserFacade.ExecutionMode.LOOSE);
    verify(printer).info(matches(".*All dependencies specified exist.*"));
    verify(printer).info(matches(".*No dependency violation detected.*"));
  }

  @Test
  public void strictAnalysisShouldProduceMetrics() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),defaultSpec);
    verifyStrictMetrics();
  }

  private void verifyStrictMetrics() {
    verify(printer).info(matches(".*Facade.*fanIn.*2.*fanOut.*3.*"),eq(1));
    verify(printer).info(matches(".*Utils.*fanIn.*2, fanOut.*0.*"),eq(1));
    verify(printer).info(matches(".*IO.*fanIn.*1.*fanOut.*3.*"),eq(1));
    verify(printer).info(matches(".*Model.*fanIn.*4.*fanOut.*0.*"),eq(1));
    verify(printer).info(matches(".*CoreInternals.*fanIn.*1.*fanOut.*3.*"),eq(1));
    verify(printer).info(matches(".*CoreApi.*fanIn.*4.*fanOut.*1.*"),eq(1));
    verify(printer).info(matches(".*Controller.*fanIn.*1.*fanOut.*1.*"),eq(1));
    verify(printer).info(matches(".*Main.*fanIn.*0.*fanOut.*4.*"),eq(1));
  }

  @Test
  public void looseAnalysisShouldProduceMetrics() {
    testee.runAnalysis(Arrays.asList(orgExamplePath),looseSpec,AnalyserFacade.ExecutionMode.LOOSE);
    verifyLooseMetrics();
  }

  private void verifyLooseMetrics() {
    verify(printer).info(matches(".*Facade.*fanIn.*2.*fanOut.*3.*"),eq(1));
    verify(printer).info(matches(".*Utils.*fanIn.*2, fanOut.*0.*"),eq(1));
    verify(printer).info(matches(".*IO.*fanIn.*1.*fanOut.*3.*"),eq(1));
    verify(printer).info(matches(".*Model.*fanIn.*4.*fanOut.*0.*"),eq(1));
    verify(printer).info(matches(".*CoreInternals.*fanIn.*1.*fanOut.*2.*"),eq(1));
    verify(printer).info(matches(".*CoreApi.*fanIn.*3.*fanOut.*1.*"),eq(1));
    verify(printer).info(matches(".*Controller.*fanIn.*1.*fanOut.*1.*"),eq(1));
    verify(printer).info(matches(".*Main.*fanIn.*0.*fanOut.*4.*"),eq(1));
  }

  @Test(expected = AnalyserException.class)
  public void strictAnalysisShouldFailAndPrintTheViolations() {
    try {
      testee.runAnalysis(Arrays.asList(orgExamplePath),wrongStrictDefinitionSpec);
    } finally {
      verify(printer).error(matches(".*The following dependencies violate the specification.*"));
      verify(printer,atLeastOnce()).error(matches(".*IO.*->.*Utils.*"),eq(1));
      verify(printer).error(matches(".*The following direct dependencies violate the specification.*"));
      verify(printer,atLeastOnce()).error(matches(".*Facade.*->.*CoreInternals.*"),eq(1));
    }
  }

  @Test(expected = AnalyserException.class)
  public void looseAnalysisShouldFailAndPrintTheViolations() {
    try {
      testee.runAnalysis(Arrays.asList(orgExamplePath),wrongLooseDefinitionSpec,AnalyserFacade.ExecutionMode.LOOSE);
    } finally {
      verify(printer).error(matches(".*The following dependencies do not exist.*"));
      verify(printer,atLeastOnce()).error(matches(".*IO.*->.*CoreInternals.*"),eq(1));
      verify(printer).error(matches(".*The following dependencies violate the specification.*"));
      verify(printer,atLeastOnce()).error(matches(".*IO.*-/->.*Model.*"),eq(1));
    }
  }

}

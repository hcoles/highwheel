package org.pitest.highwheel.ant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.report.FileStreamFactory;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class AnalyseTaskTest {

  private AnalyseTask       testee;

  @Rule
  public ExpectedException  thrown = ExpectedException.none();

  @Mock
  private AntPathParser     parser;

  @Mock
  private Target            target;

  @Mock
  private Project           project;

  @Mock
  private StreamSource      output;

  @Mock
  private FileStreamFactory fsf;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    this.testee = new AnalyseTask(this.parser, this.output);
    this.testee.setOwningTarget(this.target);
    final DirectedGraph<ElementName, Dependency> emptyGraph = new DirectedSparseGraph<ElementName, Dependency>();
    when(this.parser.parse(any(Path.class), any(Filter.class))).thenReturn(
        emptyGraph);
    when(this.target.getProject()).thenReturn(this.project);
    when(this.output.get(any(File.class))).thenReturn(this.fsf);
    when(this.fsf.getStream(anyString())).thenReturn(
        new ByteArrayOutputStream());
  }

  @Test
  public void shouldRequireUserToSupplyAFilter() {
    this.thrown.expect(BuildException.class);
    this.thrown.expectMessage("must supply a filter glob");
    this.testee.execute();
  }

  @Test
  public void shouldAnalyseSuppliedPathWhenNonEmpty() throws IOException {
    setMandatoryProperties();
    final Path p = Mockito.mock(Path.class);
    when(p.list()).thenReturn(new String[] { "foo", "bar" });
    this.testee.setAnalysisPath(p);
    this.testee.execute();
    verify(this.parser).parse(eq(p), any(Filter.class));
  }

  @Test
  public void shouldIgnoreSuppliedEmptyPaths() throws IOException {
    setMandatoryProperties();
    final Path p = Mockito.mock(Path.class);
    when(p.list()).thenReturn(new String[] { "", "" });
    this.testee.setAnalysisPath(p);
    this.testee.execute();
    verify(this.parser, never()).parse(eq(p), any(Filter.class));
  }

  @Test
  public void shouldAppendPathsWhenMultiplePathsSupplied() throws IOException {
    setMandatoryProperties();
    final Path p1 = Mockito.mock(Path.class);
    when(p1.list()).thenReturn(new String[] { "foo" });
    final Path p2 = Mockito.mock(Path.class);
    when(p2.list()).thenReturn(new String[] { "foo" });

    this.testee.setAnalysisPath(p1);
    this.testee.setAnalysisPath(p2);

    verify(p1).append(p2);
  }

  @Test
  public void shouldFilterClassPathWithSuppliedFilter() throws IOException {
    this.testee.setFilter("*.Foo.*");
    this.testee.execute();

    final ArgumentCaptor<Filter> actualFilter = ArgumentCaptor
        .forClass(Filter.class);
    verify(this.parser).parse(any(Path.class), actualFilter.capture());

    assertTrue(actualFilter.getValue().include(
        ElementName.fromString("com.Foo.bar")));
    assertFalse(actualFilter.getValue().include(
        ElementName.fromString("com.NoMatch.bar")));
  }

  @Test
  public void shouldWriteToOutputDirWhenOneSupplied() {
    setMandatoryProperties();
    final File dir = new File("foo");
    this.testee.setOutputDir(dir);
    this.testee.execute();
    verify(this.output).get(dir);
  }

  @Test
  public void shouldWriteToProjectDirWhenNoOutputDirSupplied() {
    setMandatoryProperties();
    final File dir = new File("foo");
    when(this.project.getBaseDir()).thenReturn(dir);
    this.testee.execute();
    verify(this.output).get(dir);
  }

  @Test
  public void shouldGenerateAnIndexFile() {
    setMandatoryProperties();
    this.testee.execute();
    verify(this.fsf, atLeast(1)).getStream("index.html");
  }

  private void setMandatoryProperties() {
    this.testee.setFilter("");
  }

}

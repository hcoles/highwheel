package org.pitest.highwheel.ant;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.report.FileStreamFactory;

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
  
  @Mock
  private ClasspathRoot cpr;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    this.testee = new AnalyseTask(this.parser, this.output);
    this.testee.setOwningTarget(this.target);
    //final DirectedGraph<ElementName, Dependency> emptyGraph = new DirectedSparseGraph<ElementName, Dependency>();
    when(this.parser.parse(any(Path.class))).thenReturn(cpr);
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
    verify(this.parser).parse(eq(p));
  }
  
  @Test
  public void shouldAnalyseSuppliedTestPathWhenNonEmpty() throws IOException {
    setMandatoryProperties();
    final Path p = Mockito.mock(Path.class);
    when(p.list()).thenReturn(new String[] { "foo", "bar" });
    this.testee.setTestPath(p);
    this.testee.execute();
    verify(this.parser).parse(eq(p));
  }

  @Test
  public void shouldIgnoreSuppliedEmptyPaths() throws IOException {
    setMandatoryProperties();
    final Path p = Mockito.mock(Path.class);
    when(p.list()).thenReturn(new String[] { "", "" });
    this.testee.setAnalysisPath(p);
    this.testee.execute();
    verify(this.parser, never()).parse(eq(p));
  }
  
  @Test
  public void shouldIgnoreSuppliedEmptyTestPaths() throws IOException {
    setMandatoryProperties();
    final Path p = Mockito.mock(Path.class);
    when(p.list()).thenReturn(new String[] { "", "" });
    this.testee.setTestPath(p);
    this.testee.execute();
    verify(this.parser, never()).parse(eq(p));
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
  public void shouldAppendPathsWhenMultipleTestPathsSupplied() throws IOException {
    setMandatoryProperties();
    final Path p1 = Mockito.mock(Path.class);
    when(p1.list()).thenReturn(new String[] { "foo" });
    final Path p2 = Mockito.mock(Path.class);
    when(p2.list()).thenReturn(new String[] { "foo" });

    this.testee.setTestPath(p1);
    this.testee.setTestPath(p2);

    verify(p1).append(p2);
  }
  
  @Test
  public void shouldUseReferencePath() throws IOException {
    setMandatoryProperties();
    Path p = new Path(project, "/foo");
    when(this.project.getReference("foo")).thenReturn(p);
    Reference r = new Reference(this.project, "foo");
    testee.setProject(this.project);
    testee.setAnalysisPathRef(r);
    
    this.testee.execute();

    final ArgumentCaptor<Path> actualPath = ArgumentCaptor
        .forClass(Path.class);
    verify(this.parser).parse(actualPath.capture());
    assertThat(actualPath.getValue().list()).containsOnly("/foo");
  }
  
  @Test
  public void shouldUseReferenceTestPath() throws IOException {
    setMandatoryProperties();
    Path p = new Path(project, "/foo");
    when(this.project.getReference("foo")).thenReturn(p);
    Reference r = new Reference(this.project, "foo");
    testee.setProject(this.project);
    testee.setTestPathRef(r);
    
    this.testee.execute();

    final ArgumentCaptor<Path> actualPath = ArgumentCaptor
        .forClass(Path.class);
    verify(this.parser, times(2)).parse(actualPath.capture());
    assertThat(actualPath.getAllValues().get(1).list()).containsOnly("/foo");
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

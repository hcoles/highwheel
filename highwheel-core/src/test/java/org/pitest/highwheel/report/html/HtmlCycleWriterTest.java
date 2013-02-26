package org.pitest.highwheel.report.html;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.cycles.CodeGraphs;
import org.pitest.highwheel.cycles.CodeStats;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.oracle.DependencyOracle;
import org.pitest.highwheel.report.StreamFactory;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class HtmlCycleWriterTest {
  
  private HtmlCycleWriter testee;
  

  @Mock
  private DependencyOracle      scorer;

  @Mock
  private StreamFactory         streams;

  @Mock
  private OutputStream os;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    this.os = new ByteArrayOutputStream();
    when(this.streams.getStream(anyString())).thenReturn(os);

    this.testee = new HtmlCycleWriter(this.scorer, this.streams);
  }

  @Test
  public void shouldCreateClassReport() {
    this.testee.start(emptyCodeStats());
    this.testee.end();
    verify(streams, atLeast(2)).getStream(ClassesWriter.FILENAME);
  }
  
  @Test
  public void shouldCreatePackageReport() {
    this.testee.start(emptyCodeStats());
    this.testee.end();
    verify(streams, atLeast(2)).getStream(PackagesWriter.FILENAME);
  }
  
  
  @Test
  public void shouldCreateClassCycleReport() throws SAXException, IOException {
    final DirectedSparseGraph<ElementName, Dependency> scc = smallCycle();
    this.testee.visitClassStronglyConnectedComponent(scc);
    verify(streams, atLeast(2)).getStream("class_tangle_1.html");
  }
  
  @Test
  public void shouldCreatePackageCycleReport() throws SAXException, IOException {
    final DirectedSparseGraph<ElementName, Dependency> scc = smallCycle();
    this.testee.visitPackageStronglyConnectedComponent(scc);
    verify(streams, atLeast(2)).getStream("package_tangle_1.html");
  }
  
  private DirectedSparseGraph<ElementName, Dependency> smallCycle() {
    final DirectedSparseGraph<ElementName, Dependency> scc = new DirectedSparseGraph<ElementName, Dependency>();
    final Dependency dep = new Dependency();
    scc.addEdge(dep, ElementName.fromString("foo"),
        ElementName.fromString("bar"));
    return scc;
  }
  
  private CodeStats emptyCodeStats() {
    final CodeGraphs g = new CodeGraphs(
        new DirectedSparseGraph<ElementName, Dependency>());
    return new CodeStats(g);
  }

}

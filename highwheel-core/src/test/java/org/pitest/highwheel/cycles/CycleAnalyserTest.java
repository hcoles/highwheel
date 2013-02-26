package org.pitest.highwheel.cycles;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class CycleAnalyserTest {
  
  private final static int THRESHOLD = 3;
  
  private ElementName foo = ElementName.fromString("com.foo.AClass");
  private ElementName bar = ElementName.fromString("com.bar.AClass");
  private ElementName far = ElementName.fromString("com.far.AClass");

  
  DirectedGraph<ElementName, Dependency> classGraph = new DirectedSparseGraph<ElementName, Dependency>();
  
  private  CycleAnalyser testee; 

  @Mock
  private CycleReporter r;

  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CycleAnalyser(THRESHOLD);
  }

  @Test
  public void shouldSendCodeStatsToVisitor() {
    classGraph.addEdge(dep(foo,bar, AccessType.COMPOSED), foo, bar);
    testee.analyse(new CodeGraphs(classGraph),r);
    verify(r).start(any(CodeStats.class));
  }

  
  @SuppressWarnings("unchecked")
  @Test
  public void shouldNotReportSingleItemComponents() {
    classGraph.addEdge(dep(foo,bar, AccessType.COMPOSED), foo, bar);
    testee.analyse(new CodeGraphs(classGraph),r);
    verify(r, never()).visitClassStronglyConnectedComponent(any(DirectedGraph.class));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void shouldFindAndBreakDownStronglyConnectedClassComponentsLargerThanThreshold() {
    classGraph.addEdge(dep(foo,bar, AccessType.COMPOSED), foo, bar);
    classGraph.addEdge(dep(foo,bar, AccessType.COMPOSED), bar, far);
    classGraph.addEdge(dep(foo,bar, AccessType.COMPOSED), far, foo);
    
    testee.analyse(new CodeGraphs(classGraph),r);
    verify(r).visitClassStronglyConnectedComponent(any(DirectedGraph.class));
    verify(r).endClassStronglyConnectedComponent(any(DirectedGraph.class));
    verify(r).visitClassSubCycle(any(DirectedGraph.class));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void shouldNotBreakDownSmallConnectedClassComponents() {
    classGraph.addEdge(dep(foo,bar, AccessType.COMPOSED), foo, bar);
    classGraph.addEdge(dep(foo,bar, AccessType.COMPOSED), bar, foo);
    
    testee.analyse(new CodeGraphs(classGraph),r);
    verify(r).visitClassStronglyConnectedComponent(any(DirectedGraph.class));
    verify(r, never()).visitClassSubCycle(any(DirectedGraph.class));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void shouldFindAndBreakDownStronglyConnectedPackageComponentsLargerThanThreshold() {
    classGraph.addEdge(dep(foo,bar, AccessType.COMPOSED), foo, bar);
    classGraph.addEdge(dep(bar,far, AccessType.COMPOSED), bar, far);
    classGraph.addEdge(dep(far,foo, AccessType.COMPOSED), far, foo);
    
    testee.analyse(new CodeGraphs(classGraph),r);
    verify(r).visitPackageStronglyConnectedComponent(any(DirectedGraph.class));
    verify(r).endPackageStronglyConnectedComponent(any(DirectedGraph.class));
    verify(r).visitSubCycle(any(DirectedGraph.class));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void shouldNotBreakDownSmallConnectedPackageComponents() {
    classGraph.addEdge(dep(foo,bar, AccessType.COMPOSED), foo, bar);
    classGraph.addEdge(dep(bar,foo, AccessType.COMPOSED), bar, foo);

    
    testee.analyse(new CodeGraphs(classGraph),r);
    verify(r).visitPackageStronglyConnectedComponent(any(DirectedGraph.class));
    verify(r, never()).visitSubCycle(any(DirectedGraph.class));
  }
  
  @Test
  public void shouldSignalEndOfClassCycles() {
    testee.analyse(new CodeGraphs(classGraph),r);
    verify(r).endClassCycles();
  }
  
  @Test
  public void shouldSignalEndOfAnalysis() {
    testee.analyse(new CodeGraphs(classGraph),r);
    verify(r).end();
  }

  private Dependency dep(ElementName from, ElementName to, AccessType type) {
    Dependency d = new Dependency();
    d.addDependency(AccessPoint.create(from), AccessPoint.create(to), type);
    return d;
  }
  
}

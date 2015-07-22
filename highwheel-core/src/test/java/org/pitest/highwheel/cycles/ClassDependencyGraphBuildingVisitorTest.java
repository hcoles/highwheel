package org.pitest.highwheel.cycles;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.pitest.highwheel.model.AccessType.USES;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.pitest.highwheel.model.Access;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessPointName;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class ClassDependencyGraphBuildingVisitorTest {

  
  ClassDependencyGraphBuildingVisitor testee;
  DirectedGraph<ElementName, Dependency> g;
  
  @Before
  public void setUp() {
    g = new DirectedSparseGraph<ElementName, Dependency>(); 
    testee = new ClassDependencyGraphBuildingVisitor(g);
  }
  
  @Test
  public void shouldCreateEdgesBetweenDifferentElements() {
    testee.apply(access("foo", accessPoint("a"))
               , access("notFoo", accessPoint("b"))
               , USES);
    
    assertThat(g.findEdge(element("foo"), element("notFoo"))).isNotNull();
  }
  
  @Test
  public void shouldNotCreateEdgesEqualElements() {
    testee.apply(access("foo", accessPoint("a"))
               , access("foo", accessPoint("b"))
               , USES);
    
    assertThat(g.findEdge(element("foo"), element("foo"))).isNull();
  }
  
  @Test
  public void shouldRecordAccessInEdgesWhenSingleAccess() {
    AccessPoint source = access("foo", accessPoint("a"));
    AccessPoint dest = access("bar", accessPoint("a"));
      
    testee.apply(source, dest, USES);
    
    Dependency actual = g.findEdge(source.getElementName(), dest.getElementName());
    
    assertThat(actual.consituents()).containsOnly(Access.create(source, dest, USES));
  }
  
  @Test
  public void shouldRecordAccessInEdgesWhenMultipleAccess() {
    AccessPoint source = access("foo", accessPoint("a"));
    AccessPoint firstUse = access("bar", accessPoint("a"));
    AccessPoint secondUse = access("bar", accessPoint("a"));    
      
    testee.apply(source, firstUse, USES);
    testee.apply(source, secondUse, USES);
    
    Dependency actual = g.findEdge(source.getElementName(), element("bar"));
    
    assertThat(actual.consituents()).containsAll(Arrays.asList(Access.create(source, firstUse, USES)
                                                             , Access.create(source, secondUse, USES)));
  }  
  
  @Test
  public void shouldNotCreateNodesThatHaveNoEdges() {  
    testee.newNode(element("foo"));
    assertThat(g.getVertices()).contains(element("foo"));
  }
  
  private AccessPoint access(String element, final AccessPointName point) {
    return access(element(element), point);
  }
  
  private AccessPoint access(final ElementName element, final AccessPointName point) {
    return AccessPoint.create(element, point);
  }
  
  private AccessPointName accessPoint(final String name) {
    return AccessPointName.create(name, "");
  }
  
  private ElementName element(final String type) {
    return ElementName.fromString(type);
  }


}

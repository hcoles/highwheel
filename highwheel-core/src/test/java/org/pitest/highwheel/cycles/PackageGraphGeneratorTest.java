package org.pitest.highwheel.cycles;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class PackageGraphGeneratorTest {

  @Test
  public void shouldMakeEmptyPackageGraphFromEmptyClassGraph() {
    final DirectedGraph<ElementName, Dependency> classGraph = new DirectedSparseGraph<ElementName, Dependency>();
    final DirectedGraph<ElementName, Dependency> actual = PackageGraphGenerator
        .makePackageGraph(classGraph);
    assertThat(actual.getVertices()).isEmpty();
  }
  
  @Test
  public void shouldCollapseClassDependenciesToPackageDependencies() {
    ElementName foo = ElementName.fromString("com.example.foo.AClass");
    ElementName bar = ElementName.fromString("com.example.bar.AClass");
    AccessPoint from = AccessPoint.create(foo);
    AccessPoint to = AccessPoint.create(bar);
    final DirectedGraph<ElementName, Dependency> classGraph = new DirectedSparseGraph<ElementName, Dependency>();
    classGraph.addEdge(dep(from,to),foo, bar);
    final DirectedGraph<ElementName, Dependency> actual = PackageGraphGenerator
        .makePackageGraph(classGraph);
    assertThat(actual.getVertices()).containsOnly(ElementName.fromString("com.example.foo"), ElementName.fromString("com.example.bar"));
  }

  private Dependency dep(AccessPoint from, AccessPoint to) {
    Dependency d = new Dependency();
    d.addDependency(from, to, AccessType.COMPOSED);
    return d;
  }

}

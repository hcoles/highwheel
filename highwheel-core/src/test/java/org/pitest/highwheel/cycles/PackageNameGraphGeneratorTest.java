package org.pitest.highwheel.cycles;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.pitest.highwheel.cycles.PackageNameGraphGenerator;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;

public class PackageNameGraphGeneratorTest {

  @Test
  public void shouldCreateGraphWithSingleNodeForSinglePackage() {
    final DirectedGraph<ElementName, Integer> actual = PackageNameGraphGenerator
        .generateGraph(Collections.singleton(e("com")));
    assertThat(actual.getVertices()).containsOnly(e("com"));
  }

  @Test
  public void shouldCreateGraphWithSingleEdgeForTwoLevelPackage() {
    final DirectedGraph<ElementName, Integer> actual = PackageNameGraphGenerator
        .generateGraph(Collections.singleton(e("com.example")));
    assertThat(actual.getVertices()).containsOnly(e("com"), e("com.example"));
    assertThat(actual.findEdge(e("com.example"), e("com"))).isNotNull();
  }

  @Test
  public void shouldCreateNodesWithNoEdgesForUnrelatedPackages() {
    final DirectedGraph<ElementName, Integer> actual = PackageNameGraphGenerator
        .generateGraph(Arrays.asList(e("com"), e("org")));
    assertThat(actual.getVertices()).containsOnly(e("com"), e("org"));
    assertThat(actual.getEdges()).isEmpty();
  }

  @Test
  public void shouldCreateTreeForPackageTree() {
    final DirectedGraph<ElementName, Integer> actual = PackageNameGraphGenerator
        .generateGraph(Arrays.asList(e("com.example"), e("com.example.foo"),
            e("com"), e("com.example.bar")));
    assertThat(actual.getVertices()).containsOnly(e("com"), e("com.example"),
        e("com.example.foo"), e("com.example.bar"));
    assertThat(actual.findEdge(e("com.example"), e("com"))).isNotNull();
    assertThat(actual.findEdge(e("com.example.foo"), e("com.example")))
        .isNotNull();
    assertThat(actual.findEdge(e("com.example.bar"), e("com.example")))
        .isNotNull();
  }
  
  @Test
  public void willUseIncreasingNumbersToMarkEdgesBetweenPackages() {
  
    Collection<ElementName> ps = (Arrays.asList(e("foo.bar.far"), e("foo.bar"), e("foo")));
    final DirectedGraph<ElementName, Integer> actual = PackageNameGraphGenerator
        .generateGraph(ps);
    assertThat(actual.getEdges()).containsOnly(1,0); 
  }

  private ElementName e(final String e) {
    return ElementName.fromString(e);
  }

}

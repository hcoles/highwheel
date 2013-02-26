package org.pitest.highwheel.cycles;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;

public class PackageDistanceAnalyserTest {

  private PackageDistanceAnalyser testee;

  @Before
  public void setUp() {
    final DirectedGraph<ElementName, Integer> g = makeGraph("com.example",
        "com.example.foo", "com.example.bar", "org.example.foo");
    this.testee = new PackageDistanceAnalyser(g);
  }

  @Test
  public void shouldGiveZeroDistanceForClassesInSamePackage() {
    final AccessPoint a = AccessPoint.create(ElementName
        .fromString("com.example.Foo"));
    final AccessPoint b = AccessPoint.create(ElementName
        .fromString("com.example.Bar"));
    assertThat(this.testee.distance(a, b)).isEqualTo(0);
  }

  @Test
  public void shouldGiveDistanceOf1ForClassesInParentAndChildPackages() {
    final AccessPoint a = AccessPoint.create(ElementName
        .fromString("com.example.foo.Foo"));
    final AccessPoint b = AccessPoint.create(ElementName
        .fromString("com.example.Bar"));
    assertThat(this.testee.distance(a, b)).isEqualTo(1);
  }

  @Test
  public void shouldGiveNullDistanceForUnrelatedClasses() {
    final AccessPoint a = AccessPoint.create(ElementName
        .fromString("com.example.Foo"));
    final AccessPoint b = AccessPoint.create(ElementName
        .fromString("org.example.Foo"));
    assertThat(this.testee.distance(a, b)).isNull();
  }

  @Test
  public void shouldGiveDistanceForClassesInDifferentTreeBranches() {
    final DirectedGraph<ElementName, Integer> g = makeGraph(
        "org.pitest.util.Glob", "org.pitest.functional.F");
    this.testee = new PackageDistanceAnalyser(g);
    final AccessPoint a = AccessPoint.create(ElementName
        .fromString("org.pitest.util.Glob"));
    final AccessPoint b = AccessPoint.create(ElementName
        .fromString("org.pitest.functional.F"));
    assertThat(this.testee.distance(a, b)).isEqualTo(2);
  }

  private DirectedGraph<ElementName, Integer> makeGraph(final String... ps) {
    final Collection<ElementName> elements = new ArrayList<ElementName>();
    for (final String p : ps) {
      elements.add(ElementName.fromString(p));
    }
    return PackageNameGraphGenerator.generateGraph(elements);
  }

}

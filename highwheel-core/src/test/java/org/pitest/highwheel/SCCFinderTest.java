package org.pitest.highwheel;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pitest.highwheel.algorithm.SCCFinder;
import org.pitest.highwheel.model.Cycle;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class SCCFinderTest {

  private final SCCFinder<String, Integer> testee = new SCCFinder<String, Integer>();

  @SuppressWarnings("unchecked")
  @Test
  public void shouldFindTwoSCCSInGraphWithTwoConnectedNodes() {
    final DirectedGraph<String, Integer> g = new DirectedSparseMultigraph<String, Integer>();
    g.addVertex("A");
    g.addVertex("B");
    g.addEdge(1, "A", "B");

    final List<Cycle<String>> sccs = this.testee
        .findStronglyConnectedComponents(g);
    assertThat(sccs).containsOnly(new Cycle<String>(Arrays.asList("A")),
        new Cycle<String>(Arrays.asList("B")));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldFindOneSCCSInCycleOfTwoNodes() {
    final DirectedGraph<String, Integer> g = new DirectedSparseMultigraph<String, Integer>();
    g.addVertex("A");
    g.addVertex("B");
    g.addEdge(1, "A", "B");
    g.addEdge(2, "B", "A");

    final List<Cycle<String>> sccs = this.testee
        .findStronglyConnectedComponents(g);
    assertThat(sccs).containsOnly(new Cycle<String>(Arrays.asList("A", "B")));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldFindOneSCCSInGraphWithCycleOfThreeNodes() {
    final DirectedGraph<String, Integer> g = new DirectedSparseMultigraph<String, Integer>();
    g.addVertex("A");
    g.addVertex("B");
    g.addVertex("C");
    g.addEdge(1, "A", "B");
    g.addEdge(2, "B", "C");
    g.addEdge(3, "C", "A");

    final List<Cycle<String>> sccs = this.testee
        .findStronglyConnectedComponents(g);

    assertThat(sccs).containsOnly(
        new Cycle<String>(Arrays.asList("A", "B", "C")));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldFindThreeSCCSInWikipediaExample() {
    final DirectedGraph<String, Integer> g = DirectedGraphMother
        .makeWikipediaStronglyConnectedComponentExample();

    final List<Cycle<String>> sccs = this.testee
        .findStronglyConnectedComponents(g);

    assertThat(sccs).containsOnly(
        new Cycle<String>(Arrays.asList("A", "B", "E")),
        new Cycle<String>(Arrays.asList("F", "G")),
        new Cycle<String>(Arrays.asList("C", "D", "H")));
  }

  @Test
  public void shouldFindSCCsInJot2012Graph() {
    final DirectedGraph<String, Integer> g = DirectedGraphMother
        .makeJot2012Graph();
    final List<Cycle<String>> sccs = this.testee
        .findStronglyConnectedComponents(g);

    assertEquals(3, sccs.size());
    assertThat(sccs.get(0)).containsOnly("H", "G");
    assertThat(sccs.get(1)).containsOnly("F");
    assertThat(sccs.get(2)).containsOnly("C", "E", "B", "D", "A");
  }

}

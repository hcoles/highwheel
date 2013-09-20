package org.pitest.highwheel;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;
import org.pitest.highwheel.algorithm.Cycle;
import org.pitest.highwheel.algorithm.ElementalCycleFinder;
import org.pitest.highwheel.algorithm.SCCFinder;

import edu.uci.ics.jung.graph.DirectedGraph;

public class ShortestCycleFinderTest {

  @SuppressWarnings("unchecked")
  @Test
  public void shouldFindShortestCycles() {
    final DirectedGraph<String, Integer> g = DirectedGraphMother
        .makeJot2012Graph();
    final ElementalCycleFinder<String, Integer> testee = new ElementalCycleFinder<String, Integer>(
        g);
    final SCCFinder<String, Integer> scc = new SCCFinder<String, Integer>();

    final Set<Cycle<String>> actual = testee.findShortestCycles(scc
        .findStronglyConnectedComponents(g));

    assertThat(actual).contains(new Cycle<String>(Arrays.asList("G", "H")));
    assertThat(actual)
        .contains(new Cycle<String>(Arrays.asList("B", "D", "C")));
    assertThat(actual).contains(new Cycle<String>(Arrays.asList("A", "B")));
    assertThat(actual).contains(
        new Cycle<String>(Arrays.asList("D", "E", "A", "B")));
    assertThat(actual).contains(new Cycle<String>(Arrays.asList("D", "B")));

    // we are not finding A, B C, D ,E ... why?

  }

}

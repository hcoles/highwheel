package org.pitest.highwheel.cycles;

import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SparseMultigraph;

class PackageDistanceAnalyser {

  private final UnweightedShortestPath<ElementName, Integer> usp;

  public PackageDistanceAnalyser(
      final DirectedGraph<ElementName, Integer> packageNameGraph) {
    this.usp = new UnweightedShortestPath<ElementName, Integer>(
        makeNonDirectional(packageNameGraph));
  }

  private static Hypergraph<ElementName, Integer> makeNonDirectional(
      final DirectedGraph<ElementName, Integer> packageNameGraph) {
    final SparseMultigraph<ElementName, Integer> g = new SparseMultigraph<ElementName, Integer>();
    int i = 0;
    for (final ElementName each : packageNameGraph.getVertices()) {
      g.addVertex(each);
      for (final ElementName s : packageNameGraph.getSuccessors(each)) {
        g.addEdge(i, each, s);
        i++;
      }
    }
    return g;
  }

  public Integer distance(final AccessPoint a, final AccessPoint b) {
    return (Integer) this.usp.getDistance(a.getElementName().getParent(), b
        .getElementName().getParent());
  }

}

package org.pitest.highwheel;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class DirectedGraphMother {

  /**
   * Creates the simple example graph from the paper Efficient Retrieval and
   * Ranking of Undesired Package Cycles in Large Software Systems.
   */
  public static DirectedGraph<String, Integer> makeJot2012Graph() {
    final DirectedSparseMultigraph<String, Integer> g = new DirectedSparseMultigraph<String, Integer>();

    final String A = "A";
    final String B = "B";
    final String C = "C";
    final String D = "D";
    final String E = "E";
    final String F = "F";
    final String G = "G";
    final String H = "H";

    g.addEdge(1, A, B);
    g.addEdge(2, B, A);
    g.addEdge(3, B, D);
    g.addEdge(4, D, B);
    g.addEdge(5, D, E);
    g.addEdge(6, E, A);
    g.addEdge(7, B, C);
    g.addEdge(8, C, D);
    g.addEdge(9, C, F);
    g.addEdge(10, F, G);
    g.addEdge(11, G, H);
    g.addEdge(12, H, G);

    return g;
  }

  public static DirectedGraph<String, Integer> makeWikipediaStronglyConnectedComponentExample() {
    final DirectedSparseMultigraph<String, Integer> g = new DirectedSparseMultigraph<String, Integer>();

    g.addEdge(1, "A", "B");
    g.addEdge(2, "B", "C");
    g.addEdge(3, "C", "D");
    g.addEdge(4, "D", "H");
    g.addEdge(5, "H", "G");
    g.addEdge(6, "G", "F");
    g.addEdge(7, "B", "E");
    g.addEdge(8, "E", "F");
    g.addEdge(9, "E", "A");
    g.addEdge(10, "F", "G");
    g.addEdge(11, "H", "D");
    g.addEdge(12, "D", "C");

    return g;
  }

}

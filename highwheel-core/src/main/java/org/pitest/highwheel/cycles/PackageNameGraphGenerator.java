package org.pitest.highwheel.cycles;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class PackageNameGraphGenerator {

  public static DirectedGraph<ElementName, Integer> generateGraph(
      final Collection<ElementName> packages) {
    final DirectedGraph<ElementName, Integer> g = new DirectedSparseGraph<ElementName, Integer>();
    int edgeCount = 0;

    final Collection<ElementName> sizeOrderedPackages = flattenPackages(packages);

    for (final ElementName p : sizeOrderedPackages) {
      edgeCount = addToGraph(g, edgeCount, p);
    }
    return g;
  }

  private static Collection<ElementName> flattenPackages(
      final Collection<ElementName> packages) {
    final Set<ElementName> ps = new TreeSet<ElementName>();
    for (final ElementName p : packages) {
      ps.add(p);
      ElementName parent = p.getParent();
      while (!parent.asJavaName().equals("")) {
        ps.add(parent);
        parent = parent.getParent();
      }
    }
    return ps;
  }

  private static int addToGraph(final DirectedGraph<ElementName, Integer> g,
      int edgeCount, final ElementName p) {
    final ElementName parent = p.getParent();
    if (!g.containsVertex(p) || !g.containsVertex(parent)) {
      if (!parent.asJavaName().equals("")) {
        g.addEdge(edgeCount, p, parent);
        edgeCount++;
      } else {
        g.addVertex(p);
      }

    }
    return edgeCount;
  }

}

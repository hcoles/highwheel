package org.pitest.highwheel.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pitest.highwheel.model.Cycle;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * Implements Tarjans algorithm to find strongly connected components in a
 * directed graph.
 * 
 * 
 * @param <V>
 *          Vertex
 * @param <E>
 *          Edge
 */
public class SCCFinder<V, E> {

  private final Map<V, Integer>     indexmap   = new HashMap<V, Integer>();
  private final Map<V, Integer>     lowlinkmap = new HashMap<V, Integer>();
  private final List<V>             stack      = new ArrayList<V>();
  private final List<Collection<V>> scc        = new ArrayList<Collection<V>>();

  private int                       index;

  public List<Cycle<V>> findStronglyConnectedComponents(
      final DirectedGraph<V, E> g) {

    for (final V v : g.getVertices()) {
      if (this.indexmap.get(v) == null) {
        tarjan(v, g);
      }
    }

    final List<Cycle<V>> sccs = new ArrayList<Cycle<V>>();

    for (final Collection<V> y : this.scc) {
      sccs.add(new Cycle<V>(y));
    }
    return sccs;

  }

  private void tarjan(final V v, final Graph<V, E> g) {

    this.indexmap.put(v, this.index);
    this.lowlinkmap.put(v, this.index);
    this.index++;
    this.stack.add(0, v);
    for (final E e : g.getOutEdges(v)) {
      final V n = g.getDest(e);
      if (this.indexmap.get(n) == null) {
        tarjan(n, g);
        this.lowlinkmap.put(v,
            Math.min(this.lowlinkmap.get(v), this.lowlinkmap.get(n)));
      } else if (this.stack.contains(n)) {
        this.lowlinkmap.put(v,
            Math.min(this.lowlinkmap.get(v), this.indexmap.get(n)));
      }
    }

    if (this.lowlinkmap.get(v).equals(this.indexmap.get(v))) {
      final Set<V> component = new HashSet<V>();
      V n;
      do {
        n = this.stack.remove(0);
        component.add(n);
      } while (n != v);
      this.scc.add(component);
    }

  }

}

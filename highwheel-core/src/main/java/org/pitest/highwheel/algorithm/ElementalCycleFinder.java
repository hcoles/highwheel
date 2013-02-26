package org.pitest.highwheel.algorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pitest.highwheel.model.Cycle;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Finds the elemental cycles within an SCC.
 * 
 * Based on the paper
 * "Efficient Retrieval and Ranking of Undesired Package Cycles in Large Software Systems"
 * http://www.jot.fm/issues/issue_2012_04/article4.pdf
 * 
 * @param <V>
 */
public class ElementalCycleFinder<V, E> {

  private final DirectedGraph<V, E> graph;
  private final Set<Cycle<V>>       cycles;

  public ElementalCycleFinder(final DirectedGraph<V, E> graph) {
    this.graph = graph;
    this.cycles = new HashSet<Cycle<V>>();
  }

  public Set<Cycle<V>> findShortestCycles(final List<Cycle<V>> sccs) {

    for (final Cycle<V> scc : sccs) {
      if (scc.size() > 1) {
        Map<V, Set<V>> predecessorMap = new HashMap<V, Set<V>>();
        for (final V v : scc) {
          for (final V target : getPredecessors(v)) {
            if (!predecessorMap.containsKey(target)) {
              predecessorMap.put(target, new HashSet<V>());
            }
            predecessorMap.get(target).add(v);
          }
        }
        for (final V pkg : scc) {
          this.cycles.addAll(getShortestCycles(pkg,predecessorMap));
        }
      }
    }

    return this.cycles;
  }

  private Set<Cycle<V>> getShortestCycles(final V pkg, Map<V, Set<V>> predecessorMap) {
    final Set<Cycle<V>> cycles = new HashSet<Cycle<V>>();
    final Map<V, V> prevMap = new HashMap<V, V>();

    final List<V> stack = new LinkedList<V>();
    final Set<V> closed = new HashSet<V>();
    final Set<V> opened = new HashSet<V>();

    final Set<V> ancestors = new HashSet<V>(predecessorMap.get(pkg));

    stack.add(pkg);
    opened.add(pkg);

    while (ancestors.size() > 0) {
      final V visiting = stack.remove(0);
      closed.add(visiting);
      opened.remove(visiting);

      for (final V next : getPredecessors(visiting)) {

        if (!closed.contains(next) && !opened.contains(next)) {
          prevMap.put(next, visiting);
          stack.add(next);
          opened.add(next);
        }
      }

      if (ancestors.contains(visiting)) {
        final List<V> path = new LinkedList<V>();
        for (V current = visiting; current != null; current = prevMap
            .get(current)) {
          path.add(current);
        }
        cycles.add(new Cycle<V>(path));
        ancestors.remove(visiting);
      }

    }
    return cycles;
  }

  private Collection<V> getPredecessors(final V vertex) {
    return this.graph.getPredecessors(vertex);
  }
}

package org.pitest.highwheel.cycles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Predicate;
import org.pitest.highwheel.algorithm.ElementalCycleFinder;
import org.pitest.highwheel.algorithm.SCCFinder;
import org.pitest.highwheel.model.Cycle;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Identifies interesting cycles and sub cycles within a graph
 */
public class CycleAnalyser {

  private static final int ARBITRARY_SIZE_THRESHOLD = 6;
  
  private final int cycleSubAnalysisThreshold;
  
  public CycleAnalyser() {
    this(ARBITRARY_SIZE_THRESHOLD);
  }
  
  CycleAnalyser(int cycleSubAnalysisThreshold) {
    this.cycleSubAnalysisThreshold = cycleSubAnalysisThreshold;
  }

  public void analyse(final CodeGraphs       graphs, final CycleReporter visitor) {

    generateStats(visitor, graphs);
    findClassTangles(visitor, graphs);
    visitor.endClassCycles();
    findPackageTangles(visitor, graphs);
    visitor.end();
    
  }

  private void findPackageTangles(final CycleReporter visitor, CodeGraphs graphs) {
    final List<Cycle<ElementName>> packageTangles = findStronglyConnectedComponents(graphs
        .packageGraph());
    for (final Cycle<ElementName> scc : packageTangles) {
      examinePackageTangle(visitor, scc, graphs);
    }
  }

  private void findClassTangles(final CycleReporter visitor, CodeGraphs graphs) {
    final List<Cycle<ElementName>> classTangles = findStronglyConnectedComponents(graphs
        .classGraph());
    for (final Cycle<ElementName> scc : classTangles) {
      examineClassTangle(visitor, scc, graphs);
    }
  }

  private void examineClassTangle(final CycleReporter visitor,
      final Cycle<ElementName> scc, CodeGraphs graphs) {
    final DirectedGraph<ElementName, Dependency> sccGraph = filterGraph(scc,
        graphs.classGraph());

    visitor.visitClassStronglyConnectedComponent(sccGraph);
    if (tooBigToUnderstandOnOwn(scc)) {
      final Collection<DirectedGraph<ElementName, Dependency>> subCycles = findSubCycles(
          graphs.classGraph(), scc);
      for (final DirectedGraph<ElementName, Dependency> each : subCycles) {
        visitor.visitClassSubCycle(each);
      }
    }
    visitor.endClassStronglyConnectedComponent(sccGraph);

  }

  private List<Cycle<ElementName>> findStronglyConnectedComponents(
      final DirectedGraph<ElementName, Dependency> graph) {
    final SCCFinder<ElementName, Dependency> sccf = new SCCFinder<ElementName, Dependency>();
    final List<Cycle<ElementName>> sccs = sccf
        .findStronglyConnectedComponents(graph);
    final List<Cycle<ElementName>> largeSccs = new ArrayList<Cycle<ElementName>>();
    for (final Cycle<ElementName> each : sccs) {
      if (each.size() > 1) {
        largeSccs.add(each);
      }
    }
    return largeSccs;

  }

  private void examinePackageTangle(final CycleReporter visitor,
      final Cycle<ElementName> scc, CodeGraphs graphs) {
    final DirectedGraph<ElementName, Dependency> sccGraph = filterGraph(scc,
        graphs.packageGraph());
    visitor.visitPackageStronglyConnectedComponent(sccGraph);
    if (tooBigToUnderstandOnOwn(scc)) {
      final Collection<DirectedGraph<ElementName, Dependency>> subCycles = findSubCycles(
          graphs.packageGraph(), scc);
      for (final DirectedGraph<ElementName, Dependency> each : subCycles) {
        visitor.visitSubCycle(each);
      }
    }
    visitor.endPackageStronglyConnectedComponent(sccGraph);

  }

  private void generateStats(final CycleReporter visitor, CodeGraphs graphs) {
    final CodeStats stats = new CodeStats(graphs);
    visitor.start(stats);
  }

  private boolean tooBigToUnderstandOnOwn(final Cycle<ElementName> scc) {
    return scc.size() >= cycleSubAnalysisThreshold;
  }

  private Collection<DirectedGraph<ElementName, Dependency>> findSubCycles(
      final DirectedGraph<ElementName, Dependency> graph,
      final Cycle<ElementName> scc) {
    final DirectedGraph<ElementName, Dependency> sccGraph = filterGraph(scc,
        graph);

    final ElementalCycleFinder<ElementName, Dependency> subCycleFinder = new ElementalCycleFinder<ElementName, Dependency>(
        sccGraph);
    final Set<Cycle<ElementName>> subCycles = subCycleFinder
        .findShortestCycles(Collections.singletonList(scc));

    final Collection<DirectedGraph<ElementName, Dependency>> subCycleGraphs = toGraphs(
        subCycles, sccGraph);

    return subCycleGraphs;

  }

  private Collection<DirectedGraph<ElementName, Dependency>> toGraphs(
      final Set<Cycle<ElementName>> subCycles,
      final DirectedGraph<ElementName, Dependency> sccGraph) {
    final Collection<DirectedGraph<ElementName, Dependency>> gs = new ArrayList<DirectedGraph<ElementName, Dependency>>();
    for (final Cycle<ElementName> each : subCycles) {
      gs.add(filterGraph(each, sccGraph));
    }
    return gs;
  }

  private DirectedGraph<ElementName, Dependency> filterGraph(
      final Cycle<ElementName> each,
      final DirectedGraph<ElementName, Dependency> graph) {
    final Predicate<ElementName> p = new Predicate<ElementName>() {
      public boolean evaluate(final ElementName arg0) {
        return each.contains(arg0);
      }

    };
    final VertexPredicateFilter<ElementName, Dependency> f = new VertexPredicateFilter<ElementName, Dependency>(
        p);
    return (DirectedGraph<ElementName, Dependency>) f.transform(graph);
  }

}

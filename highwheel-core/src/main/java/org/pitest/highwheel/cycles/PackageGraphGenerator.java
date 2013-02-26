package org.pitest.highwheel.cycles;

import org.pitest.highwheel.model.Access;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class PackageGraphGenerator {

  public static DirectedGraph<ElementName, Dependency> makePackageGraph(
      final DirectedGraph<ElementName, Dependency> classGraph) {

    final DirectedGraph<ElementName, Dependency> transformed = new DirectedSparseGraph<ElementName, Dependency>();
    final PackageGraphBuildingDependencyVisitor v = new PackageGraphBuildingDependencyVisitor(
        transformed);
    for (final Dependency each : classGraph.getEdges()) {
      for (final Access access : each.consituents()) {
        v.apply(access.getSource(), access.getDest(), access.getType());
      }
    }
    return transformed;

  }

}

package org.pitest.highwheel.cycles;

import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;

public class CodeGraphs {

  private final DirectedGraph<ElementName, Integer>    packageNameGraph;
  private final DirectedGraph<ElementName, Dependency> classGraph;
  private final DirectedGraph<ElementName, Dependency> packageGraph;

  public CodeGraphs(final DirectedGraph<ElementName, Dependency> classGraph) {
    this.classGraph = classGraph;
    this.packageGraph = PackageGraphGenerator.makePackageGraph(classGraph);
    this.packageNameGraph = PackageNameGraphGenerator
        .generateGraph(this.packageGraph.getVertices());
  }

  public DirectedGraph<ElementName, Integer> packageNameGraph() {
    return this.packageNameGraph;
  }

  public DirectedGraph<ElementName, Dependency> classGraph() {
    return this.classGraph;
  }

  public DirectedGraph<ElementName, Dependency> packageGraph() {
    return this.packageGraph;
  }

}

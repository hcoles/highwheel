package org.pitest.highwheel.cycles;


import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;

class PackageGraphBuildingDependencyVisitor implements AccessVisitor {

  private final DirectedGraph<ElementName, Dependency> g;

  public PackageGraphBuildingDependencyVisitor(
      final DirectedGraph<ElementName, Dependency> g) {
    this.g = g;
  }

  public void apply(final AccessPoint source, final AccessPoint dest,
      final AccessType type) {
    final ElementName sourcePackage = source.getElementName().getParent();
    final ElementName destPackage = dest.getElementName().getParent();

    if (!sourcePackage.equals(destPackage)) {
      Dependency edge = this.g.findEdge(sourcePackage, destPackage);
      if (edge == null) {
        edge = new Dependency();
        this.g.addEdge(edge, sourcePackage, destPackage);
      }
      edge.addDependency(source, dest, type);
    }

  }

  public void newNode(final ElementName clazz) {
    this.g.addVertex(clazz.getParent());

  }

  public void newEntryPoint(final ElementName clazz) {
    // TODO Auto-generated method stub

  }

}

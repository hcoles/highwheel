package org.pitest.highwheel.cycles;

import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;

public class ClassDependencyGraphBuildingVisitor implements AccessVisitor {

  private final DirectedGraph<ElementName, Dependency> g;


  public ClassDependencyGraphBuildingVisitor(
      final DirectedGraph<ElementName, Dependency> g) {
    this.g = g;
  }

  public void apply(final AccessPoint source, final AccessPoint dest,
      final AccessType type) {
    final ElementName sourceClass = source.getElementName();
    final ElementName destClass = dest.getElementName();

    if (!sourceClass.equals(destClass)) {
      Dependency edge = this.g.findEdge(sourceClass, destClass);
      if (edge == null) {
        edge = new Dependency();
        this.g.addEdge(edge, sourceClass, destClass);
      }
      edge.addDependency(source, dest, type);
    }

    // update edge here
  }

  public DirectedGraph<ElementName, Dependency> getGraph() {
    return this.g;
  }

  public void newNode(final ElementName clazz) {
    this.g.addVertex(clazz);
  }

  public void newEntryPoint(final ElementName clazz) {

  }

}
package org.pitest.highwheel.cycles;

import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;

public interface CycleReporter {

  void start(CodeStats stats);

  void visitClassSubCycle(DirectedGraph<ElementName, Dependency> each);

  void visitClassStronglyConnectedComponent(
      DirectedGraph<ElementName, Dependency> sccGraph);

  void visitPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc);

  void visitSubCycle(final DirectedGraph<ElementName, Dependency> cycle);

  void endPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc);

  void endClassCycles();

  void endClassStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc);

  void end();

}

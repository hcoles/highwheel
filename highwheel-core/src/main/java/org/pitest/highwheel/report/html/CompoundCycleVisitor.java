package org.pitest.highwheel.report.html;

import java.util.ArrayList;
import java.util.Collection;

import org.pitest.highwheel.cycles.CodeStats;
import org.pitest.highwheel.cycles.CycleReporter;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;

public class CompoundCycleVisitor implements CycleReporter {
  private final Collection<CycleReporter> children = new ArrayList<CycleReporter>();

  public CompoundCycleVisitor(final Collection<CycleReporter> cvs) {
    this.children.addAll(cvs);
  }

  public void visitPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    for (final CycleReporter cv : this.children) {
      cv.visitPackageStronglyConnectedComponent(scc);
    }

  }

  public void visitSubCycle(final DirectedGraph<ElementName, Dependency> cycle) {
    for (final CycleReporter cv : this.children) {
      cv.visitSubCycle(cycle);
    }
  }

  public void end() {
    for (final CycleReporter cv : this.children) {
      cv.end();
    }
  }

  public void start(final CodeStats stats) {
    for (final CycleReporter cv : this.children) {
      cv.start(stats);
    }
  }

  public void visitClassSubCycle(final DirectedGraph<ElementName, Dependency> g) {
    for (final CycleReporter cv : this.children) {
      cv.visitClassSubCycle(g);
    }
  }

  public void visitClassStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> g) {
    for (final CycleReporter cv : this.children) {
      cv.visitClassStronglyConnectedComponent(g);
    }
  }

  public void endPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    for (final CycleReporter cv : this.children) {
      cv.endPackageStronglyConnectedComponent(scc);
    }

  }

  public void endClassStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    for (final CycleReporter cv : this.children) {
      cv.endClassStronglyConnectedComponent(scc);
    }

  }

  public void endClassCycles() {
    for (final CycleReporter cv : this.children) {
      cv.endClassCycles();
    }
  }

}

package org.pitest.highwheel.report.html;

import org.pitest.highwheel.cycles.CycleReporter;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.report.StreamFactory;

import edu.uci.ics.jung.graph.DirectedGraph;

abstract class BaseWriter extends BaseHtmlWriter implements CycleReporter {

  private int packageSccCount = 0;
  private int classSccCount   = 0;

  BaseWriter(final StreamFactory streams) {
    super(streams);
  }

  public final void visitPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    this.packageSccCount++;
    visitPackageScc(scc);
  }

  public final void visitClassStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    this.classSccCount++;
    visitClassScc(scc);
  }

  protected abstract void visitClassScc(
      DirectedGraph<ElementName, Dependency> scc);

  protected abstract void visitPackageScc(
      DirectedGraph<ElementName, Dependency> scc);

  protected int getCurrentPackageSccNumber() {
    return this.packageSccCount;
  }

  protected String getCurrentPackageSccName() {
    return "package_tangle_" + this.packageSccCount + ".html";
  }

  protected int getCurrentClassSccNumber() {
    return this.classSccCount;
  }

  protected String getCurrentClassSccName() {
    return "class_tangle_" + this.classSccCount + ".html";
  }
}

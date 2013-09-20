package org.pitest.highwheel.report.html;

import org.pitest.highwheel.cycles.CodeStats;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.oracle.DependencyOracle;
import org.pitest.highwheel.report.StreamFactory;

import edu.uci.ics.jung.graph.DirectedGraph;

public class IndexWriter extends BaseWriter {

  public final static String INDEX = "index.html";

  public IndexWriter(final DependencyOracle dependencyScorer,
      final StreamFactory streams) {
    super(streams);
  }

  public void start(final CodeStats stats) {
    writeHeader(INDEX);
    writeIndex("<header>");
    writeIndex("<h1>Highwheel report</h1>");
    writeIndex("</header>");

    writeIndex("<section>");
    writeIndex("<ul>");
    writeIndex("<li><a href=\"" + ClassesWriter.FILENAME + "\">"
        + stats.getClassCount() + " classes</a></li>");
    writeIndex("<li><a href=\"" + PackagesWriter.FILENAME + "\">"
        + stats.getPackageCount() + " packages</a></li>");
    writeIndex("<ul>");
    writeIndex("</section>");

    writeIndex("<section><h1>Class cycles</h1><ul>");

  }

  @Override
  public void visitPackageScc(final DirectedGraph<ElementName, Dependency> scc) {
    final String sccFile = getCurrentPackageSccName();
    final ElementName firstPackage = scc.getVertices().iterator().next();
    writeIndex("<li><a href=\"" + sccFile + "\">" + firstPackage + " and "
        + (scc.getVertexCount() - 1) + " others</a></li>");
  }

  public void visitSubCycle(final DirectedGraph<ElementName, Dependency> cycle) {

  }

  public void end() {
    writeIndex("</ul></section>");
    this.writeFooter(INDEX);
  }

  private void writeIndex(final String value) {
    write(INDEX, value);
  }

  public void visitClassSubCycle(
      final DirectedGraph<ElementName, Dependency> each) {

  }

  public void endPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {

  }

  public void endClassStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {

  }

  @Override
  protected void visitClassScc(final DirectedGraph<ElementName, Dependency> scc) {
    final String sccFile = this.getCurrentClassSccName();
    final ElementName firstClass = scc.getVertices().iterator().next();
    writeIndex("<li><a href=\"" + sccFile + "\">" + firstClass + " and "
        + (scc.getVertexCount() - 1) + " others</a></li>");

  }

  public void endClassCycles() {
    writeIndex("</ul></section>");
    writeIndex("<section><h1>Packages cycles</h1><ul>");

  }


}

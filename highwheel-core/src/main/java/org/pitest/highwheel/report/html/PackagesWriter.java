package org.pitest.highwheel.report.html;

import org.pitest.highwheel.cycles.CodeStats;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.report.StreamFactory;

import edu.uci.ics.jung.graph.DirectedGraph;

class PackagesWriter extends BaseWriter {

  public final static String FILENAME = "packages.html";

  PackagesWriter(final StreamFactory streams) {
    super(streams);
  }

  public void start(final CodeStats stats) {
    writeHeader(FILENAME);

    write(FILENAME, "<section class ='deps'>");
    write(FILENAME, "<table id=\"sorttable\" class=\"tablesorter\"><thead><tr><th>package</th><th>influence</th></tr></thead>");
    write(FILENAME, "<tbody>");
    for (final ElementName each : stats.getPackages()) {
      write(FILENAME,
          "<tr><td>" + each + "</td><td>"
              + stats.getPackageStats(each).getPageRank() + "</td></tr>");
    }
    write(FILENAME, "</tbody>");
    write(FILENAME, "</table>");
    write(FILENAME, "</section");
    writeFooter(FILENAME);
  }

  public void visitClassSubCycle(
      final DirectedGraph<ElementName, Dependency> each) {
    // TODO Auto-generated method stub

  }

  public void visitSubCycle(final DirectedGraph<ElementName, Dependency> cycle) {
    // TODO Auto-generated method stub

  }

  public void endPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    // TODO Auto-generated method stub

  }

  public void endClassStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    // TODO Auto-generated method stub

  }

  public void end() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void visitClassScc(final DirectedGraph<ElementName, Dependency> scc) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void visitPackageScc(
      final DirectedGraph<ElementName, Dependency> scc) {
    // TODO Auto-generated method stub

  }

  public void endClassCycles() {
    // TODO Auto-generated method stub

  }

}

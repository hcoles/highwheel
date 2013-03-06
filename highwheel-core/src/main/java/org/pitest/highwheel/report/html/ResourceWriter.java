package org.pitest.highwheel.report.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pitest.highwheel.cycles.CodeStats;
import org.pitest.highwheel.cycles.CycleReporter;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.report.StreamFactory;
import org.pitest.highwheel.util.StreamUtil;

import edu.uci.ics.jung.graph.DirectedGraph;

class ResourceWriter implements CycleReporter {

  private final StreamFactory streams;

  ResourceWriter(final StreamFactory streams) {
    this.streams = streams;
  }

  public void start(final CodeStats stats) {
    writeResource("style.css");
    writeResource("jquery-latest.js");
    writeResource("jquery.tablesorter.min.js");    
    writeResource("asc.gif");
    writeResource("bg.gif");
    writeResource("desc.gif");
  }

  private void writeResource(final String resource) {
    // context class loader does not resolve resource when running via ant task
    final InputStream is = getClass().getClassLoader().
        getResourceAsStream(resource);

    try {
      final OutputStream os = this.streams.getStream(resource);
      StreamUtil.copy(is, os);
      os.close();
      is.close();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitClassSubCycle(
      final DirectedGraph<ElementName, Dependency> each) {
    // TODO Auto-generated method stub

  }

  public void visitClassStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> sccGraph) {
    // TODO Auto-generated method stub

  }

  public void visitPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    // TODO Auto-generated method stub

  }

  public void visitSubCycle(final DirectedGraph<ElementName, Dependency> cycle) {
    // TODO Auto-generated method stub

  }

  public void endPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    // TODO Auto-generated method stub

  }

  public void endClassCycles() {
    // TODO Auto-generated method stub

  }

  public void endClassStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    // TODO Auto-generated method stub

  }

  public void end() {
    // TODO Auto-generated method stub

  }

}

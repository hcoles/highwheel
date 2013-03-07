package org.pitest.highwheel.report.html;

import java.io.IOException;

import org.pitest.highwheel.cycles.CodeStats;
import org.pitest.highwheel.model.Access;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.oracle.DependencyOracle;
import org.pitest.highwheel.report.StreamFactory;
import org.pitest.highwheel.report.svg.SVGExporter;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

class CycleWriter extends BaseWriter {

  private CodeStats              stats;
  private final DependencyOracle dependencyScorer;

  CycleWriter(final DependencyOracle dependencyScorer,
      final StreamFactory streams) {
    super(streams);
    this.dependencyScorer = dependencyScorer;
  }

  public void start(final CodeStats stats) {
    this.stats = stats;
  }

  public void visitSubCycle(final DirectedGraph<ElementName, Dependency> cycle) {
    final String streamName = getCurrentPackageSccName();
    writeSubCycle(cycle, streamName);
  }

  private void writeSubCycle(
      final DirectedGraph<ElementName, Dependency> cycle,
      final String streamName) {
    write(streamName, "<section class='subcycle'>");
    write(streamName, "<header>");
    write(streamName, "<h1>Subcycle : " + cycle.getVertexCount() + " members "
        + cycle.getEdgeCount() + " connections </h1>");
    write(streamName, "</header>");

    final SVGExporter ex = new SVGExporter(this.streams.getStream(streamName),
        this.dependencyScorer, xsize(cycle), ysize(cycle));
    try {

      write(streamName, "<figure>");
      ex.export(cycle);
      write(streamName, "</figure>");

      writeConnections(streamName, cycle);
      
      write(streamName, "<a href='#classConnections'>jump to class connections</a>");

      write(streamName, "</section>");
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private int xsize(final DirectedGraph<ElementName, Dependency> cycle) {
    return Math.max(600, cycle.getVertexCount() * 90);
  }

  private int ysize(final DirectedGraph<ElementName, Dependency> cycle) {
    return Math.max(100, cycle.getVertexCount() * 90);
  }

  public void end() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void visitPackageScc(
      final DirectedGraph<ElementName, Dependency> scc) {
    writeScc(scc, getCurrentPackageSccName(), this.getCurrentPackageSccNumber());
  }

  private void writeConnections(final String stream,
      final DirectedGraph<ElementName, Dependency> cycle) {
    write(stream, "<section class='cons'>");

    write(stream, "<table><thead><tr><th>from</th><th>to</th></thead></tr>");

    for (final Dependency d : cycle.getEdges()) {
      final Pair<ElementName> edge = cycle.getEndpoints(d);
      write(stream,
          "<tr><td>" + edge.getFirst() + "</td><td>" + edge.getSecond()
              + "</td>");
      write(stream, "</tr>");

    }
    write(stream, "</table>");
    write(stream, "</section>");
  }

  private void writeClassConnections(final String stream,
      final DirectedGraph<ElementName, Dependency> cycle) {
    write(stream, "<section id='classConnections' class='deps'>");
    write(stream, "<h1>Class dependencies</h1>");
    write(stream, "<a name='classConnections'/>");
    write(stream, "<table id=\"sorttable\" class=\"tablesorter\">");
    write(
        stream,
        "<thead><tr><th>distance</th><th>from</th><th>type</th><th>to</th></tr></thead>");
    write(stream, "<tbody>");
    for (final Dependency element : cycle.getEdges()) {
      for (final Access each : element.consituents()) {
        write(stream, "<tr><td>" + showNumber(this.stats.getDistance(each))
            + "</td><td>" + each.getSource() + "</td><td>" + each.getType()
            + "</td><td>" + each.getDest() + "</td></tr>");
      }
    }
    write(stream, "</tbody>");
    write(stream, "</table>");
    write(stream, "</section>");

  }

  private String showNumber(final Integer i) {
    if (i == null) {
      return "&infin;";
    }

    return "" + i;

  }

  @Override
  protected void visitClassScc(final DirectedGraph<ElementName, Dependency> scc) {
    final String sccFile = getCurrentClassSccName();
    final int number = this.getCurrentClassSccNumber();
    writeScc(scc, sccFile, number);
  }

  private void writeScc(final DirectedGraph<ElementName, Dependency> scc,
      final String sccFile, final int number) {
    writeHeader(sccFile);
    write(sccFile, "<section>");
    write(sccFile, "<header>");
    write(sccFile, "<h1>Component " + number + "</h1>");
    write(sccFile,
        "<h2>" + scc.getVertexCount() + " members " + scc.getEdgeCount()
            + " connections </h2>");
    write(sccFile, "</header>");

    final SVGExporter ex = new SVGExporter(this.streams.getStream(sccFile),
        this.dependencyScorer, xsize(scc), ysize(scc));

    try {
      write(sccFile, "<figure>");
      ex.export(scc);
      write(sccFile, "</figure>");
      writeConnections(sccFile, scc);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitClassSubCycle(
      final DirectedGraph<ElementName, Dependency> cycle) {
    writeSubCycle(cycle, this.getCurrentClassSccName());
  }

  public void endPackageStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    write(this.getCurrentPackageSccName(), "</section>");
    writeClassConnections(getCurrentPackageSccName(), scc);
    this.writeFooter(this.getCurrentPackageSccName());
  }

  public void endClassStronglyConnectedComponent(
      final DirectedGraph<ElementName, Dependency> scc) {
    write(this.getCurrentPackageSccName(), "</section>");
    writeClassConnections(getCurrentClassSccName(), scc);
    this.writeFooter(this.getCurrentClassSccName());
  }

  public void endClassCycles() {
    // TODO Auto-generated method stub

  }

}

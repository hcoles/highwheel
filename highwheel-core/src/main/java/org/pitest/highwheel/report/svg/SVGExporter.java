package org.pitest.highwheel.report.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.pitest.highwheel.model.Access;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.oracle.DependencyOracle;
import org.pitest.highwheel.oracle.DependendencyStatus;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

/**
 * Generates SVG representations of graphs via JUNG
 * 
 */
public class SVGExporter {

  private final OutputStream     out;
  private final DependencyOracle dependencyScorer;
  private final int              width;
  private final int              height;

  public SVGExporter(final OutputStream w,
      final DependencyOracle dependencyScorer, final int width, final int height) {
    this.out = w;
    this.width = width;
    this.height = height;
    this.dependencyScorer = dependencyScorer;
  }

  public void export(final DirectedGraph<ElementName, Dependency> g)
      throws IOException {

    final Dimension d = new Dimension(this.width, this.height);
    final Layout<ElementName, Dependency> layout = pickLayout(g, d);

    final VisualizationImageServer<ElementName, Dependency> viz = new VisualizationImageServer<ElementName, Dependency>(
        layout, d);

    final RenderContext<ElementName, Dependency> context = viz
        .getRenderContext();
    context.setEdgeLabelTransformer(new EdgeLabeller());
    context.setVertexLabelTransformer(new VertexLabeller());

    final VertexRenderer vertexRenderer = new VertexRenderer(
        viz.getRenderContext());

    viz.getRenderContext().setVertexShapeTransformer(vertexRenderer);
    viz.getRenderer().setVertexLabelRenderer(vertexRenderer);

    context.setVertexFillPaintTransformer(fillVertex());

    context.setEdgeDrawPaintTransformer(fillEdge(g));

    context.setEdgeLabelClosenessTransformer(closenessTransformer());
    context.setEdgeStrokeTransformer(edgeWeight());
    context.setEdgeArrowStrokeTransformer(edgeWeight());
    context.setArrowFillPaintTransformer(fillEdge(g));
    context.setArrowDrawPaintTransformer(fillEdge(g));

    viz.setPreferredSize(new Dimension(this.width, this.height));
    viz.setBackground(new Color(1, 1, 1, 0));

    final SVGGraphics2D svgGenerator = new SVGGraphics2D(this.out, d);
    svgGenerator.writeHeader();
    viz.paintAll(svgGenerator);
    svgGenerator.writeTrailer();
    svgGenerator.dispose();

  }

  private Transformer<Dependency, Paint> fillEdge(
      final DirectedGraph<ElementName, Dependency> g) {
    return new Transformer<Dependency, Paint>() {
      public Paint transform(final Dependency d) {
        final float alpha = 0.5f;
        final DependendencyStatus status = getWorseStatus(d);
        switch (status) {
        case OK:
          return new Color(0, 1, 0, alpha);
        case FORBIDDEN:
          return new Color(1, 0, 0, alpha);
        default:
          return new Color(0, 0, 0, alpha);
        }

      }

    };
  }

  DependendencyStatus getWorseStatus(final Dependency d) {
    DependendencyStatus s = DependendencyStatus.UNKNOWN;
    for (final Access each : d.consituents()) {
      final DependendencyStatus newS = this.dependencyScorer.assess(each);
      if (newS == DependendencyStatus.FORBIDDEN) {
        return newS;
      }

      if (newS.lessDesirableThan(s)) {
        s = newS;
      }

    }

    return s;

  }

  private static Transformer<ElementName, Paint> fillVertex() {
    return new Transformer<ElementName, Paint>() {

      public Paint transform(final ElementName arg0) {
        final float alpha = 1.0f;
        return new Color(1, 1, 1, alpha);
      }
    };
  }

  private Transformer<Dependency, Stroke> edgeWeight() {
    return new Transformer<Dependency, Stroke>() {
      public Stroke transform(final Dependency arg0) {
        return new BasicStroke(Math.min(arg0.getStrength(), 4), // Line width
            BasicStroke.CAP_ROUND, // End-cap style
            BasicStroke.JOIN_ROUND); // Vertex join style
      }

    };
  }

  private Transformer<Context<Graph<ElementName, Dependency>, Dependency>, Number> closenessTransformer() {
    return new Transformer<Context<Graph<ElementName, Dependency>, Dependency>, Number>() {

      public Number transform(
          final Context<Graph<ElementName, Dependency>, Dependency> arg0) {
        return 0.35;
      }

    };
  }

  private Layout<ElementName, Dependency> pickLayout(
      final DirectedGraph<ElementName, Dependency> g, final Dimension d) {
    if ( g.getVertexCount() == 2 ) {
      return linearLayout(g,d);
    }
    else if (g.getVertexCount() <= 20) {
      return circleLayout(g, d);
    }
    return springLayout(g, d);
  }

  private Layout<ElementName, Dependency> linearLayout(
      DirectedGraph<ElementName, Dependency> g, Dimension d) {
    Map<ElementName, Point2D> map = new HashMap<ElementName, Point2D>();
    Iterator<ElementName> it = g.getVertices().iterator();
    map.put(it.next(), new Point2D.Double(100d, d.getHeight()/2));
    map.put(it.next(), new Point2D.Double(d.getWidth() - 100d, d.getHeight()/2));
    Transformer<ElementName,Point2D> vertexLocations =
        TransformerUtils.mapTransformer(map);
    return new StaticLayout<ElementName, Dependency>(g, vertexLocations, d);
  }

  private Layout<ElementName, Dependency> springLayout(
      final DirectedGraph<ElementName, Dependency> g, final Dimension d) {
    final Layout<ElementName, Dependency> l = new SpringLayout2<ElementName, Dependency>(
        g);
    l.setSize(new Dimension(d.width - 100, d.height - 150));
    shift(l, 50, 20);
    return l;
  }

  private Layout<ElementName, Dependency> circleLayout(
      final DirectedGraph<ElementName, Dependency> g, final Dimension d) {
    final CircleLayout<ElementName, Dependency> l = new CircleLayout<ElementName, Dependency>(
        g);
    l.setSize(new Dimension(d.width - 100, d.height - 60));
    shift(l, 60, 50);
    return l;
  }

  private void shift(final Layout<ElementName, Dependency> l, final int dx,
      final int dy) {
    for (final ElementName each : l.getGraph().getVertices()) {
      final Point2D point = l.transform(each);
      point.setLocation(point.getX() + dx, point.getY() + dy);
    }
  }
}

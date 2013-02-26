package org.pitest.highwheel.report.svg;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.commons.collections15.Transformer;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class VertexRenderer implements
    Renderer.VertexLabel<ElementName, Dependency>,
    Transformer<ElementName, Shape> {

  private static final RoundRectangle2D                theRoundRectangle = new RoundRectangle2D.Float();

  private final RenderContext<ElementName, Dependency> rc;

  public VertexRenderer(final RenderContext<ElementName, Dependency> rc) {
    this.rc = rc;
  }

  public Component prepareRenderer(
      final RenderContext<ElementName, Dependency> rc,
      final VertexLabelRenderer graphLabelRenderer, final Object value,
      final boolean isSelected, final ElementName vertex) {
    return rc.getVertexLabelRenderer()
        .<ElementName> getVertexLabelRendererComponent(rc.getScreenDevice(),
            value, rc.getVertexFontTransformer().transform(vertex), isSelected,
            vertex);
  }

  public void labelVertex(final RenderContext<ElementName, Dependency> rc,
      final Layout<ElementName, Dependency> layout, final ElementName v,
      final String label) {
    final Graph<ElementName, Dependency> graph = layout.getGraph();
    if (rc.getVertexIncludePredicate().evaluate(
        Context.<Graph<ElementName, Dependency>, ElementName> getInstance(
            graph, v)) == false) {
      return;
    }
    final GraphicsDecorator g = rc.getGraphicsContext();
    final Component component = prepareRenderer(rc,
        rc.getVertexLabelRenderer(), label,
        rc.getPickedVertexState().isPicked(v), v);
    final Dimension d = component.getPreferredSize();

    final int h_offset = -d.width / 2;
    final int v_offset = -d.height / 2;

    Point2D p = layout.transform(v);
    p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);

    final int x = (int) p.getX();
    final int y = (int) p.getY();

    g.draw(component, rc.getRendererPane(), x + h_offset, y + v_offset,
        d.width, d.height, true);

  }

  public Shape transform(final ElementName v) {

    final Component component = prepareRenderer(this.rc,
        this.rc.getVertexLabelRenderer(), this.rc.getVertexLabelTransformer()
            .transform(v), this.rc.getPickedVertexState().isPicked(v), v);
    final Dimension size = component.getPreferredSize();
    final int dimx = Math.max(size.width, 70);
    final int dimy = 70;
    final Rectangle bounds = new Rectangle((-dimx / 2) - 2,
        ((-dimy / 2) / 2) - 2, dimx + 4, dimy / 2);

    final Rectangle2D frame = bounds;
    final float arc_size = (float) Math
        .min(frame.getHeight(), frame.getWidth()) / 2;
    theRoundRectangle.setRoundRect(frame.getX(), frame.getY(),
        frame.getWidth(), frame.getHeight(), arc_size, arc_size);
    return theRoundRectangle;
  }

  public Renderer.VertexLabel.Position getPosition() {
    return Renderer.VertexLabel.Position.CNTR;
  }

  public Renderer.VertexLabel.Positioner getPositioner() {
    return new Positioner() {
      public Renderer.VertexLabel.Position getPosition(final float x,
          final float y, final Dimension d) {
        return Renderer.VertexLabel.Position.CNTR;
      }
    };
  }

  public void setPosition(final Renderer.VertexLabel.Position position) {

  }

  public void setPositioner(final Renderer.VertexLabel.Positioner positioner) {

  }
}

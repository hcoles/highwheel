package org.pitest.highwheel.cycles;

import java.util.HashMap;
import java.util.Map;

import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedGraph;

class VertexStats {

  private final Map<ElementName, VertexStatistic> classStats   = new HashMap<ElementName, VertexStatistic>();
  private final Map<ElementName, VertexStatistic> packageStats = new HashMap<ElementName, VertexStatistic>();

  public VertexStats(final CodeGraphs graphs) {
    generateStats(graphs.classGraph(), this.classStats);
    generateStats(graphs.packageGraph(), this.packageStats);
  }

  private void generateStats(final DirectedGraph<ElementName, Dependency> g,
      final Map<ElementName, VertexStatistic> m) {
    final PageRank<ElementName, Dependency> pr = new PageRank<ElementName, Dependency>(
        g, 0.1f);
    pr.evaluate();
    for (final ElementName v : g.getVertices()) {
      m.put(v,
          new VertexStatistic((int) Math.round(1000 * pr.getVertexScore(v))));
    }

  }

  public VertexStatistic getClassStats(final ElementName clazz) {
    return this.classStats.get(clazz);
  }

  public VertexStatistic getPackageStats(final ElementName pkg) {
    return this.packageStats.get(pkg);
  }

}

package org.pitest.highwheel.cycles;

import java.util.Collection;

import org.pitest.highwheel.model.Access;
import org.pitest.highwheel.model.ElementName;

public class CodeStats {

  private final CodeGraphs              g;
  private final VertexStats             vertexStats;
  private final PackageDistanceAnalyser distance;

  public CodeStats(final CodeGraphs g) {
    this.g = g;
    this.vertexStats = new VertexStats(g);
    this.distance = new PackageDistanceAnalyser(g.packageNameGraph());
  }

  public VertexStatistic getClassStats(final ElementName clazz) {
    return this.vertexStats.getClassStats(clazz);
  }

  public VertexStatistic getPackageStats(final ElementName pkg) {
    return this.vertexStats.getPackageStats(pkg);
  }

  public Collection<ElementName> getClasses() {
    return this.g.classGraph().getVertices();
  }

  public int getClassCount() {
    return getClasses().size();
  }

  public int getPackageCount() {
    return getPackages().size();
  }

  public Collection<ElementName> getPackages() {
    return this.g.packageGraph().getVertices();
  }

  public Integer getDistance(final Access each) {
    return this.distance.distance(each.getSource(), each.getDest());
  }

}

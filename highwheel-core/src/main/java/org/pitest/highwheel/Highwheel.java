package org.pitest.highwheel;

import java.io.IOException;

import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.classpath.ClassParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.ClassDependencyGraphBuildingVisitor;
import org.pitest.highwheel.cycles.CodeGraphs;
import org.pitest.highwheel.cycles.CycleAnalyser;
import org.pitest.highwheel.cycles.CycleReporter;
import org.pitest.highwheel.losttests.LostTestAnalyser;
import org.pitest.highwheel.losttests.LostTestHTMLVisitor;
import org.pitest.highwheel.losttests.LostTestVisitor;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.oracle.DependencyOracle;
import org.pitest.highwheel.report.FileStreamFactory;
import org.pitest.highwheel.report.html.HtmlCycleWriter;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class Highwheel {

  private final DependencyOracle  dependencyOracle;
  private final FileStreamFactory fsf;
  private final ClassParser       parser;

  public Highwheel(final ClassParser parser,
      final DependencyOracle dependencyOracle, final FileStreamFactory fsf) {
    this.dependencyOracle = dependencyOracle;
    this.fsf = fsf;
    this.parser = parser;
  }

  public void analyse(final ClasspathRoot mainRoot, final ClasspathRoot testRoot)
      throws IOException {

    final DirectedGraph<ElementName, Dependency> classGraph = new DirectedSparseGraph<ElementName, Dependency>();

    final AccessVisitor v = new ClassDependencyGraphBuildingVisitor(classGraph);

    this.parser.parse(mainRoot, v);

    final CodeGraphs g = new CodeGraphs(classGraph);
    final CycleAnalyser cycleAnalyser = new CycleAnalyser();
    final CycleReporter r = new HtmlCycleWriter(this.dependencyOracle, this.fsf);
    cycleAnalyser.analyse(g, r);

    if (testRoot != null) {
      LostTestVisitor visitor = new LostTestHTMLVisitor(this.fsf);
      final LostTestAnalyser lostTestAnalyser = new LostTestAnalyser();
      lostTestAnalyser.analyse(mainRoot, testRoot,visitor);
    }
  }

}

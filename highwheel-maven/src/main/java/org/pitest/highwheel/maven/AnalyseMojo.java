package org.pitest.highwheel.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.pitest.highwheel.bytecodeparser.ClassPathParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.AccessVisitor;
import org.pitest.highwheel.cycles.ClassGraphBuildingDependencyVisitor;
import org.pitest.highwheel.cycles.CodeGraphs;
import org.pitest.highwheel.cycles.CycleAnalyser;
import org.pitest.highwheel.cycles.CycleReporter;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.oracle.DependencyOracle;
import org.pitest.highwheel.oracle.DependendencyStatus;
import org.pitest.highwheel.oracle.FixedScorer;
import org.pitest.highwheel.oracle.SimpleFlatFileOracleParser;
import org.pitest.highwheel.report.FileStreamFactory;
import org.pitest.highwheel.report.html.HtmlCycleWriter;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * 
 * @goal analyse
 * 
 * @requiresDependencyResolution compile
 * 
 */
public class AnalyseMojo extends BaseMojo {


  /**
   * Location of user defined access rules
   * 
   * @parameter
   */
  private String       accessRules;

  

  @Override
  protected void analyse(final ClasspathRoot cpr, final Filter filter)
      throws MojoExecutionException {
    try {

      final ClassPathParser parser = new ClassPathParser(cpr, filter);

      final DirectedGraph<ElementName, Dependency> classGraph = new DirectedSparseGraph<ElementName, Dependency>();

      final AccessVisitor v = new ClassGraphBuildingDependencyVisitor(
          classGraph);

      parser.parse(v);

      this.getLog().info("Scanned " + classGraph.getVertexCount() + " classes");

      runAnalysis(classGraph);

    } catch (final IOException ex) {
      throw new MojoExecutionException("Error while scanning codebase", ex);
    }
  }


  private void runAnalysis(
      final DirectedGraph<ElementName, Dependency> classGraph) throws FileNotFoundException,
      IOException {

    final DependencyOracle dependencyOracle = makePackageScorer();
    final CodeGraphs g = new CodeGraphs(classGraph);
    final CycleAnalyser analyser = new CycleAnalyser();

    final File dir = makeReportDirectory("highwheel");
    final FileStreamFactory fsf = new FileStreamFactory(dir);
    final CycleReporter r = new HtmlCycleWriter(dependencyOracle, fsf);
    analyser.analyse(g, r);
    fsf.close();
  }

  private DependencyOracle makePackageScorer() throws IOException {
    if (this.accessRules != null) {
      final InputStream is = new FileInputStream(this.accessRules);
      try {
        return new SimpleFlatFileOracleParser(is).parse();
      } finally {
        is.close();
      }
    }
    return new FixedScorer(DependendencyStatus.UNKNOWN);
  }

}
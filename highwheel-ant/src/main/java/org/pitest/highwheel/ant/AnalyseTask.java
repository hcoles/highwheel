package org.pitest.highwheel.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.pitest.highwheel.bytecodeparser.ClassPathParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.classpath.CompoundClassPathRoot;
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
import org.pitest.highwheel.util.GlobToRegex;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class AnalyseTask extends Task {

  private Filter filter;
  private Path   analysisPath;
  private String accessRules;

  public void setFilter(final String glob) {
    this.filter = makeFilter(glob);
  }

  @Override
  public void execute() throws BuildException {
    super.execute();

    if (this.filter == null) {
      throw new BuildException("must supply a filter glob");
    }

    try {
      analyse();
    } catch (final IOException e) {
      throw new BuildException(e);
    }

  }

  private void analyse() throws IOException {
    final List<ClasspathRoot> roots = ClassPath
        .createRoots(getClassPathElements());
    final ClassPathParser parser = new ClassPathParser(
        new CompoundClassPathRoot(roots), this.filter);

    final DirectedGraph<ElementName, Dependency> classGraph = new DirectedSparseGraph<ElementName, Dependency>();

    final long t0 = System.currentTimeMillis();
    final AccessVisitor v = new ClassGraphBuildingDependencyVisitor(classGraph);
    final long t1 = System.currentTimeMillis();
    final long dt = (t1 - t0) / 1000;

    parser.parse(v);

    this.log("Scanned " + classGraph.getVertexCount() + " classes in " + dt
        + " seconds");

    visualiseGraph(classGraph);

  }

  private Collection<File> getClassPathElements() {
    final Collection<File> files = new ArrayList<File>();
    for (final String each : this.analysisPath.list()) {
      files.add(new File(each));
    }
    return files;
  }

  private void visualiseGraph(
      final DirectedGraph<ElementName, Dependency> classGraph)
      throws FileNotFoundException, IOException {

    final DependencyOracle dependencyOracle = this.makePackageOracle();
    final CodeGraphs g = new CodeGraphs(classGraph);
    final CycleAnalyser analyser = new CycleAnalyser();

    final FileStreamFactory fos = new FileStreamFactory(getOwningTarget()
        .getProject().getBaseDir());

    final CycleReporter r = new HtmlCycleWriter(dependencyOracle, fos);
    analyser.analyse(g, r);

    fos.close();
  }

  private DependencyOracle makePackageOracle() throws IOException {
    if (this.accessRules != null) {
      final FileInputStream is = new FileInputStream(new File(getProject()
          .getBaseDir(), this.accessRules));
      try {
        return new SimpleFlatFileOracleParser(is).parse();
      } finally {
        is.close();
      }
    }
    return new FixedScorer(DependendencyStatus.UNKNOWN);
  }

  public void setAnalysisPath(final Path src) {
    if (isNonEmpty(src)) {
      if (this.analysisPath == null) {
        this.analysisPath = src;
      } else {
        this.analysisPath.append(src);
      }
    }
  }


  public Path createAnalysisPath() {
    if (this.analysisPath == null) {
      this.analysisPath = new Path(getProject());
    }
    return this.analysisPath.createPath();
  }

  public void setAnalysisPathRef(final Reference r) {
    final Path path = createAnalysisPath();
    path.setRefid(r);
    path.toString(); // throws on error
  }


  private boolean isNonEmpty(final Path src) {
    for (final String anElementList : src.list()) {
      if (!anElementList.equals("")) {
        return true;
      }
    }
    return false;
  }

  private Filter makeFilter(final String glob) {
    final Pattern p = Pattern.compile(GlobToRegex.convertGlobToRegex(glob));
    return new Filter() {
      public boolean include(final ElementName item) {
        return p.matcher(item.asJavaName()).matches();
      }

    };
  }

  public void setAccessRules(final String accessRules) {
    this.accessRules = accessRules;
  }

}
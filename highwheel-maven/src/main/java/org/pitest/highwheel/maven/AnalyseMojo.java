package org.pitest.highwheel.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.pitest.highwheel.bytecodeparser.ClassPathParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.classpath.CompoundClassPathRoot;
import org.pitest.highwheel.classpath.DirectoryClassPathRoot;
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

/**
 * 
 * @goal analyse
 * 
 * @requiresDependencyResolution compile
 * 
 */
public class AnalyseMojo extends AbstractMojo {

  /**
   * <i>Internal</i>: Project to interact with.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Classes to include in analyse. Glob syntax
   * 
   * @parameter
   */
  private String       classFilter;

  /**
   * Location of user defined access rules
   * 
   * @parameter
   */
  private String       accessRules;

  
  /**
   * Analyse only parent (i.e pom) projects
   * 
   * @parameter default="false" expression="${parentOnly}"
   */
  private boolean parentOnly;
  
  /**
   * Analyse only child projects i.e do not analyse parent projects
   * 
   * @parameter default="false" expression="${childOnly}"
   */
  private boolean childOnly;



  public void execute() throws MojoExecutionException, MojoFailureException {

    String packaging = this.project.getModel().getPackaging();
    
    if ( packaging.equalsIgnoreCase("pom") && this.childOnly ) {
      this.getLog().info("Skipping pom project");
      return;
    }
    
    if ( !packaging.equalsIgnoreCase("pom") && this.parentOnly ) {
      this.getLog().info("Skipping non pom project");
      return;
    }
    
    final Filter filter = createClassFilter();

    final List<ClasspathRoot> roots = collectRootsForChildProjects();
    if (!packaging.equalsIgnoreCase("pom")) {
      roots.add(makeRootForProject(this.project));
    }

    final CompoundClassPathRoot cpr = new CompoundClassPathRoot(roots);

    analyse(cpr, filter);

  }

  private List<ClasspathRoot> collectRootsForChildProjects() {
    final List<ClasspathRoot> roots = new ArrayList<ClasspathRoot>();
    for (final Object each : this.project.getCollectedProjects()) {
      final MavenProject project = (MavenProject) each;
      this.getLog().info("Including child project " + project.getName());
      roots.add(makeRootForProject(project));
    }
    return roots;
  }

  private ClasspathRoot makeRootForProject(final MavenProject project) {
    File f = new File(project.getBuild()
        .getOutputDirectory());
    if ( !f.exists() || !f.isDirectory() ) {
      this.getLog().warn("Cannot read from " + f.getAbsolutePath());
    }
    this.getLog().info("Including dir " + project.getBuild()
        .getOutputDirectory());
    return new DirectoryClassPathRoot(new File(project.getBuild()
        .getOutputDirectory()));
  }

  private void analyse(final ClasspathRoot cpr, final Filter filter)
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




  private Filter createClassFilter() {
    if (this.classFilter == null) {
      return makeFilter(this.project.getGroupId() + ".*");
    }
    return makeFilter(this.classFilter);
  }

  private Filter makeFilter(final String glob) {
    final Pattern p = Pattern.compile(GlobToRegex.convertGlobToRegex(glob));
    return new Filter() {
      public boolean include(final ElementName item) {
        return p.matcher(item.asJavaName()).matches();
      }

    };
  }

  private void runAnalysis(
      final DirectedGraph<ElementName, Dependency> classGraph) throws FileNotFoundException,
      IOException {

    final DependencyOracle dependencyOracle = makePackageScorer();
    final CodeGraphs g = new CodeGraphs(classGraph);
    final CycleAnalyser analyser = new CycleAnalyser();

    final File dir = new File(this.project.getBuild().getDirectory()
        + File.separator + "highwheel");
    dir.mkdirs();
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
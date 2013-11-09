package org.pitest.highwheel.bytecode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.pitest.highwheel.bytecodeparser.classpath.CompoundClassPathRoot;
import org.pitest.highwheel.bytecodeparser.classpath.DirectoryClassPathRoot;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.util.GlobToRegex;

/**
 * Base mojo for analysis mojos that require a class filter
 *  
 */
public abstract class BaseMojo extends AbstractMojo {

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
   * @parameter default="" expression="${classFilter}"
   */
  private String       classFilter;

  
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



  public final void execute() throws MojoExecutionException, MojoFailureException {

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

  protected abstract void analyse(final ClasspathRoot cpr, final Filter filter) throws MojoExecutionException;


  private Filter createClassFilter() {
    if (this.classFilter == null || this.classFilter.isEmpty() ) {
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

  protected File makeReportDirectory(String dirName) {
    final File dir = new File(this.project.getBuild().getDirectory()
        + File.separator + dirName);
    dir.mkdirs();
    return dir;
  }


}
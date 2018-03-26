package org.pitest.highwheel.maven;

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

//FIXME duplicated from highwheel-maven as properties fail
//to set when included as dependency

/**
 * Base mojo for analysis mojos that require a class filter
 *
 */
public abstract class BaseMojo extends AbstractMojo {

  /**
   * <i>Internal</i>: Project to interact with.
   * 
   * @parameter property="project"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Classes to include in analyse. Glob syntax
   * 
   * @parameter default="" property="classFilter"
   */
  private String       classFilter;

  /**
   * Analyse only parent (i.e pom) projects
   * 
   * @parameter default="false" property="parentOnly"
   */
  private boolean      parentOnly;

  /**
   * Analyse only child projects i.e do not analyse parent projects
   * 
   * @parameter default="false" property="childOnly"
   */
  private boolean      childOnly;

  public final void execute() throws MojoExecutionException,
      MojoFailureException {

    final String packaging = this.project.getModel().getPackaging();

    if (packaging.equalsIgnoreCase("pom") && this.childOnly) {
      this.getLog().info("Skipping pom project");
      return;
    }

    if (!packaging.equalsIgnoreCase("pom") && this.parentOnly) {
      this.getLog().info("Skipping non pom project");
      return;
    }

    final Filter filter = createClassFilter();

    final ClasspathRoot mainRoot = makeRoot(packaging, mainDir());
    final ClasspathRoot testRoot = makeRoot(packaging, testDir());

    analyse(mainRoot, testRoot, filter);

  }

  private static F<MavenProject, File> testDir() {
    return new F<MavenProject, File>() {
      public File apply(final MavenProject a) {
        return new File(a.getBuild().getTestOutputDirectory());
      }
    };
  }

  private CompoundClassPathRoot makeRoot(final String packaging,
      final F<MavenProject, File> dirFunc) {
    final List<ClasspathRoot> roots = collectRootsForChildProjects(dirFunc);
    if (!packaging.equalsIgnoreCase("pom")) {
      roots.add(makeRootForProject(this.project, dirFunc));
    }
    return new CompoundClassPathRoot(roots);
  }

  private static F<MavenProject, File> mainDir() {
    return new F<MavenProject, File>() {
      public File apply(final MavenProject a) {
        return new File(a.getBuild().getOutputDirectory());
      }
    };
  }

  private List<ClasspathRoot> collectRootsForChildProjects(
      final F<MavenProject, File> dirFunc) {
    final List<ClasspathRoot> roots = new ArrayList<ClasspathRoot>();
    for (final Object each : this.project.getCollectedProjects()) {
      final MavenProject project = (MavenProject) each;
      this.getLog().info("Including child project " + project.getName());
      roots.add(makeRootForProject(project, dirFunc));
    }
    return roots;
  }

  private ClasspathRoot makeRootForProject(final MavenProject project,
      final F<MavenProject, File> dirFunc) {
    final File f = dirFunc.apply(project);
    if (!f.exists() || !f.isDirectory()) {
      this.getLog().warn("Cannot read from " + f.getAbsolutePath());
    }
    this.getLog().info(
        "Including dir " + f);
    return new DirectoryClassPathRoot(f);
  }

  protected abstract void analyse(final ClasspathRoot mainRoot,
      final ClasspathRoot testRoot, final Filter filter)
      throws MojoExecutionException;

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

  protected File makeReportDirectory(final String dirName) {
    final File dir = new File(this.project.getBuild().getDirectory()
        + File.separator + dirName);
    dir.mkdirs();
    return dir;
  }

}
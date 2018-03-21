package org.pitest.highwheel.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.pitest.highwheel.modules.AnalyserFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mojo(name = "analyse")
public class ModuleAnalyserMojo extends AbstractMojo {

  private class MavenPrinter implements  AnalyserFacade.Printer {

    private final String INDENTATION = "  ";

    @Override
    public void info(String msg, int indentation) {
      getLog().info(indent(indentation) + msg);
    }

    private String indent(int num) {
      final StringBuilder builder = new StringBuilder("");
      for(int i =0; i < num; ++i) {
        builder.append(INDENTATION);
      }
      return builder.toString();
    }

    @Override
    public void info(String msg) {
      getLog().info(msg);
    }

    @Override
    public void error(String msg, int indentation) {
      getLog().error(indent(indentation) + msg);
    }

    @Override
    public void error(String msg) {
      getLog().error(msg);
    }

    @Override
    public void warning(String msg) {
      getLog().warn(msg);
    }
  }

  @Parameter(property = "project", readonly = true, required = true)
  private MavenProject project;

  @Parameter(property = "parentOnly", defaultValue = "false")
  private boolean parentOnly;

  @Parameter(property = "childOnly", defaultValue = "false")
  private boolean childOnly;

  @Parameter(property = "specFile", defaultValue = "spec.hwm")
  private String specFile;

  @Parameter(property = "analysisMode", defaultValue = "strict")
  private String analysisMode;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final String packaging = project.getModel().getPackaging();

    if (packaging.equalsIgnoreCase("pom") && childOnly) {
      this.getLog().info("Skipping pom project");
      return;
    }

    if (!packaging.equalsIgnoreCase("pom") && parentOnly) {
      this.getLog().info("Skipping non pom project");
      return;
    }
    final List<String> roots = getRootsForProject(packaging);
    final AnalyserFacade.ExecutionMode executionMode = getExecutionMode(analysisMode);
    analyse(roots,executionMode,specFile);
  }

  private List<String> getRootsForProject(final String packaging) {
    final List<String> childrenRoots = collectRootsForChildProjects();
    final List<String> roots = new ArrayList<>(childrenRoots);
    if (!packaging.equalsIgnoreCase("pom")) {
      roots.add(makeRootForProject(this.project));
    }
    return roots;
  }

  private List<String> collectRootsForChildProjects() {
    final List<String> roots = new ArrayList<>();
    for (final Object each : this.project.getCollectedProjects()) {
      final MavenProject project = (MavenProject) each;
      this.getLog().info("Including child project " + project.getName());
      roots.add(makeRootForProject(project));
    }
    return roots;
  }

  private String makeRootForProject(final MavenProject project) {
    return project.getBuild().getOutputDirectory();
  }

  private AnalyserFacade.ExecutionMode getExecutionMode(String analysisMode) throws MojoExecutionException {
    if(Objects.equals(analysisMode, "strict")) {
      return AnalyserFacade.ExecutionMode.STRICT;
    } else if(Objects.equals(analysisMode,"loose")) {
      return AnalyserFacade.ExecutionMode.LOOSE;
    } else {
      throw new MojoExecutionException("Parameter 'analysisMode' needs to be either 'strict' or 'loose''");
    }
  }

  private void analyse(List<String> roots, AnalyserFacade.ExecutionMode executionMode, String specFilePath) throws MojoFailureException {
    try {
      final AnalyserFacade.Printer printer = new MavenPrinter();
      final AnalyserFacade facade = new AnalyserFacade(printer);
      facade.runAnalysis(roots,specFilePath,executionMode);
    } catch(Exception e) {
      throw new MojoFailureException("Error during analysis: " + e.getMessage());
    }
  }
}

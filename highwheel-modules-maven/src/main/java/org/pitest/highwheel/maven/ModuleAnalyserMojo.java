package org.pitest.highwheel.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.pitest.highwheel.modules.AnalyserFacade;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.pitest.highwheel.util.StringUtil.join;

@Mojo(name = "analyse")
public class ModuleAnalyserMojo extends AbstractMojo {

  private class MavenPrinter implements  AnalyserFacade.Printer {

    @Override
    public void info(String msg) {
      getLog().info(msg);
    }
  }


  private class MavenPathEventSink implements AnalyserFacade.EventSink.PathEventSink {

    @Override
    public void ignoredPaths(List<String> ignored) {
      final String ignoredString = "Ignored: " + join(", ", ignored);
      if(ignored.isEmpty()) {
        getLog().info(ignoredString);
      } else {
        getLog().warn(ignoredString);
      }
    }

    @Override
    public void directories(List<String> directories) {
      getLog().info("Directories: " + join(", ", directories));
    }

    @Override
    public void jars(List<String> jars) {
      getLog().info("Jars: " + join(", ", jars));
    }
  }

  private class MavenMeasureSink implements AnalyserFacade.EventSink.MeasureEventSink {

    @Override
    public void fanInOutMeasure(String module, int fanIn, int fanOut) {
      getLog().info(String.format("  %20s --> fanIn: %5d, fanOut: %5d", module, fanIn, fanOut));
    }
  }

  private class MavenStrictAnalysisSink implements AnalyserFacade.EventSink.StrictAnalysisEventSink {

    @Override
    public void dependenciesCorrect() {
      getLog().info("No dependency violation detected");
    }

    @Override
    public void directDependenciesCorrect() {
      getLog().info("No direct dependency violation detected");
    }

    @Override
    public void dependencyViolationsPresent() {
      getLog().error("The following dependencies violate the specification:");
    }

    @Override
    public void dependencyViolation(String sourceModule, String destModule, List<String> expectedPath,
        List<String> actualPath) {
      getLog().error(String.format("  %s -> %s. Expected path: %s, Actual path: %s",
          sourceModule,
          destModule,
          printGraphPath(expectedPath),
          printGraphPath(actualPath)
      ));
    }

    @Override
    public void noDirectDependenciesViolationPresent() {
      getLog().error("The following direct dependencies violate the specification:");
    }

    @Override
    public void noDirectDependencyViolation(String sourceModule, String destModule) {
      getLog().error(String.format("  %s -> %s",
          sourceModule,
          destModule
      ));
    }
  }

  private class MavenLooseAnalysisEventSink implements AnalyserFacade.EventSink.LooseAnalysisEventSink {

    @Override
    public void allDependenciesPresent() {
      getLog().info(" - All dependencies specified exist");
    }

    @Override
    public void noUndesiredDependencies() {
      getLog().info(" - No dependency violation detected");
    }

    @Override
    public void absentDependencyViolationsPresent() {
      getLog().error(" - The following dependencies do not exist:");
    }

    @Override
    public void absentDependencyViolation(String sourceModule, String destModule) {
      getLog().error(String.format("  %s -> %s",
          sourceModule,
          destModule
      ));
    }

    @Override
    public void undesiredDependencyViolationsPresent() {
      getLog().error(" - The following dependencies violate the specification:");
    }

    @Override
    public void undesiredDependencyViolation(String sourceModule, String destModule, List<String> path) {
      getLog().error(String.format("  %s -/-> %s. Actual path: %s",
          sourceModule,
          destModule,
          printGraphPath(path)
      ));
    }
  }

  private static String printGraphPath(List<String> pathComponents) {
    if(pathComponents.isEmpty()) {
      return "(empty)";
    } else {
      return join(" -> ", pathComponents);
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
    final File attemptSpecFileInBuild = new File(project.getBasedir().getAbsolutePath() + File.separator + specFile);
    if (attemptSpecFileInBuild.exists() && attemptSpecFileInBuild.canRead()) {
      specFile = attemptSpecFileInBuild.getAbsolutePath();
      getLog().info("Using specification file: " + specFile);
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
      final AnalyserFacade facade = new AnalyserFacade(printer, new MavenPathEventSink(),
          new MavenMeasureSink(), new MavenStrictAnalysisSink(), new MavenLooseAnalysisEventSink());
      facade.runAnalysis(roots,specFilePath,executionMode);
    } catch(Exception e) {
      throw new MojoFailureException("Error during analysis: " + e.getMessage());
    }
  }
}

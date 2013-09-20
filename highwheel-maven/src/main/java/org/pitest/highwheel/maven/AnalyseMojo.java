package org.pitest.highwheel.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.pitest.highwheel.Highwheel;
import org.pitest.highwheel.bytecodeparser.ClassPathParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.oracle.DependencyOracle;
import org.pitest.highwheel.oracle.DependendencyStatus;
import org.pitest.highwheel.oracle.FixedScorer;
import org.pitest.highwheel.oracle.SimpleFlatFileOracleParser;
import org.pitest.highwheel.report.FileStreamFactory;

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
  private String accessRules;

  @Override
  protected void analyse(final ClasspathRoot mainRoot,
      final ClasspathRoot testRoot, final Filter filter)
      throws MojoExecutionException {
    try {

      final ClassPathParser parser = new ClassPathParser(filter);

      final File dir = makeReportDirectory("highwheel");
      final FileStreamFactory fsf = new FileStreamFactory(dir);

      try {
        final Highwheel a = new Highwheel(parser, makePackageScorer(), fsf);
        a.analyse(mainRoot, testRoot);
      } finally {
        fsf.close();
      }

    } catch (final IOException ex) {
      throw new MojoExecutionException("Error while scanning codebase", ex);
    }
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
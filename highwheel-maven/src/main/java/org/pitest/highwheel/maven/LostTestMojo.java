package org.pitest.highwheel.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.ElementName;

/**
 * 
 * @goal lostTests
 * 
 * @requiresDependencyResolution test
 * 
 */
public class LostTestMojo extends BaseMojo {

  @Override
  protected void analyse(final ClasspathRoot mainRoot,
      final ClasspathRoot testRoot, final Filter filter)
      throws MojoExecutionException {
    final TesteeGuesser testeeGuesser = new TesteeGuesser(mainRoot);
    System.out.println("Scanning " + testRoot.classNames().size() + " tests");
    for (final ElementName each : mainRoot.classNames()) {
      final ElementName testee = testeeGuesser.guessTestee(each);
      if (testee != null) {
        if (!testee.getParent().equals(each.getParent())) {
          System.out.println(each
              + " may be in wrong package. Consider moving to "
              + testee.getParent() + " where possible testee " + testee
              + " lives");
        }
      }
    }

  }

}

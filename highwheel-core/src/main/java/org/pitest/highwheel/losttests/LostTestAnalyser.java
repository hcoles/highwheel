package org.pitest.highwheel.losttests;

import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;

public class LostTestAnalyser {

  public void analyse(final ClasspathRoot mainRoot,
      final ClasspathRoot testRoot) {
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

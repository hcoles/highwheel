package org.pitest.highwheel.losttests;

import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;

public class LostTestAnalyser {

  public void analyse(final ClasspathRoot mainRoot,
      final ClasspathRoot testRoot, LostTestVisitor visitor) {
    final TesteeGuesser testeeGuesser = new TesteeGuesser(mainRoot);
    visitor.start();
    for (final ElementName each : testRoot.classNames()) {
      final ElementName testee = testeeGuesser.guessTestee(each);
      if (testee != null) {
        if (!testee.getParent().equals(each.getParent())) {
          visitor.visitLostTest(each, testee);
        }
      }
    }
    visitor.end();

  }
  
}

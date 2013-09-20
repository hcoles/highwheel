package org.pitest.highwheel.losttests;

import java.util.HashSet;

import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;

public class TesteeGuesser {

  private final HashSet<ElementName> classes = new HashSet<ElementName>();

  public TesteeGuesser(final ClasspathRoot mainRoot) {
    this.classes.addAll(mainRoot.classNames());
  }

  public ElementName guessTestee(final ElementName test) {
    final ElementName testee = guessTesteeFromName(test);
    if (testee == null) {
      return null;
    }

    if (classExists(testee)) {
      return testee;
    }
    return findClassNamed(testee.getNameWithoutPackage());
  }

  private ElementName findClassNamed(final ElementName nameWithoutPackage) {
    for (final ElementName each : this.classes) {
      if (each.getNameWithoutPackage().equals(nameWithoutPackage)) {
        return each;
      }
    }
    return null;
  }

  private boolean classExists(final ElementName testee) {
    return this.classes.contains(testee);
  }

  private ElementName guessTesteeFromName(final ElementName test) {
    final String name = test.getNameWithoutPackage().asJavaName();
    if (name.startsWith("Test")) {
      return ElementName.fromString(test.getParent().asJavaName() + "."
          + name.replaceFirst("Test", ""));
    }

    if (name.endsWith("Test")) {
      return ElementName.fromString(test.getParent().asJavaName() + "."
          + name.substring(0, name.length() - "Test".length()));
    }

    return null;
  }

}

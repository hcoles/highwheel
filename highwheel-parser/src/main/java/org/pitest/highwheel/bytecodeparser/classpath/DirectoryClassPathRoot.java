package org.pitest.highwheel.bytecodeparser.classpath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;

public class DirectoryClassPathRoot implements ClasspathRoot {

  private final File root;

  public DirectoryClassPathRoot(final File root) {
    this.root = root;
  }

  public InputStream getData(final ElementName classname) throws IOException {
    return getResource(classname.asJavaName().replace('.', File.separatorChar)
        .concat(".class"));
  }

  public InputStream getResource(final String name) throws IOException {
    final File file = new File(this.root, name);
    if (file.canRead()) {
      return new FileInputStream(file);
    } else {
      return null;
    }
  }

  public Collection<ElementName> classNames() {
    return classNames(this.root);
  }

  private Collection<ElementName> classNames(final File file) {
    final List<ElementName> classNames = new LinkedList<ElementName>();

    if (!file.exists() || !file.isDirectory() ) {
      return Collections.emptyList();
    }
    
    for (final File f : file.listFiles()) {
      if (f.isDirectory()) {
        classNames.addAll(classNames(f));
      } else if (f.getName().endsWith(".class")) {
        classNames.add(fileToClassName(f));
      }
    }
    return classNames;
  }

  private ElementName fileToClassName(final File f) {
    return ElementName.fromString(f
        .getAbsolutePath()
        .substring(this.root.getAbsolutePath().length() + 1,
            (f.getAbsolutePath().length() - ".class".length()))
        .replace(File.separatorChar, '.'));
  }

}

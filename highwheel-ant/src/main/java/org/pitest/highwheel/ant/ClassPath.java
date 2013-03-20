package org.pitest.highwheel.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.highwheel.classpath.ArchiveClassPathRoot;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.classpath.DirectoryClassPathRoot;

public class ClassPath {

  public static List<ClasspathRoot> createRoots(final Collection<File> files) {
    final List<ClasspathRoot> rs = new ArrayList<ClasspathRoot>();
    for (final File f : files) {
      if (f.isDirectory()) {
        rs.add(new DirectoryClassPathRoot(f));
      } else {
        if (!f.canRead()) {
          throw new RuntimeException("Can't read the file " + f);
        }
        rs.add(new ArchiveClassPathRoot(f));
      }
    }
    return rs;
  }

}

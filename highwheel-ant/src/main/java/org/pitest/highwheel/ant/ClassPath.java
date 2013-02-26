package org.pitest.highwheel.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipException;

import org.pitest.highwheel.classpath.ArchiveClassPathRoot;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.classpath.DirectoryClassPathRoot;

public class ClassPath {

  public static List<ClasspathRoot> createRoots(final Collection<File> files) {
    File lastFile = null;
    try {
      final List<ClasspathRoot> rs = new ArrayList<ClasspathRoot>();

      for (final File f : files) {
        lastFile = f;
        if (f.isDirectory()) {
          rs.add(new DirectoryClassPathRoot(f));
        } else {
          try {
            if (!f.canRead()) {
              throw new IOException("Can't read the file " + f);
            }
            rs.add(new ArchiveClassPathRoot(f));
          } catch (final ZipException ex) {
            System.err.println("Can't open the archive " + f);
          }
        }
      }
      return rs;
    } catch (final IOException ex) {
      throw new RuntimeException("Error handling file " + lastFile, ex);
    }
  }

}

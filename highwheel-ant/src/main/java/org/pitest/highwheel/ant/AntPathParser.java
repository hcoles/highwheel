package org.pitest.highwheel.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tools.ant.types.Path;
import org.pitest.highwheel.bytecodeparser.classpath.CompoundClassPathRoot;
import org.pitest.highwheel.classpath.ClasspathRoot;

class AntPathParser {

  ClasspathRoot parse(final Path analysisPath) {
    final List<ClasspathRoot> roots = ClassPath
        .createRoots(getClassPathElements(analysisPath));
    return new CompoundClassPathRoot(roots);
  }

  private Collection<File> getClassPathElements(final Path analysisPath) {
    final Collection<File> files = new ArrayList<File>();
    for (final String each : analysisPath.list()) {
      files.add(new File(each));
    }
    return files;
  }

}

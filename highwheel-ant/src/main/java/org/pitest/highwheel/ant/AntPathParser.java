package org.pitest.highwheel.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tools.ant.types.Path;
import org.pitest.highwheel.bytecodeparser.ClassPathParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.classpath.CompoundClassPathRoot;
import org.pitest.highwheel.cycles.AccessVisitor;
import org.pitest.highwheel.cycles.ClassGraphBuildingDependencyVisitor;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.Dependency;
import org.pitest.highwheel.model.ElementName;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

class AntPathParser {

  DirectedGraph<ElementName, Dependency> parse(final Path analysisPath,
      final Filter filter) throws IOException {
    final List<ClasspathRoot> roots = ClassPath
        .createRoots(getClassPathElements(analysisPath));
    final ClassPathParser parser = new ClassPathParser(
        new CompoundClassPathRoot(roots), filter);

    final DirectedGraph<ElementName, Dependency> classGraph = new DirectedSparseGraph<ElementName, Dependency>();
    final AccessVisitor v = new ClassGraphBuildingDependencyVisitor(classGraph);

    parser.parse(v);
    return classGraph;
  }

  private Collection<File> getClassPathElements(final Path analysisPath) {
    final Collection<File> files = new ArrayList<File>();
    for (final String each : analysisPath.list()) {
      files.add(new File(each));
    }
    return files;
  }

}

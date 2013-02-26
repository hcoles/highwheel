package org.pitest.highwheel.bytecodeparser;


import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.AccessVisitor;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.ElementName;

public class ClassPathParser {

  private final Filter        filter;
  private final ClasspathRoot cp;

  public ClassPathParser(final ClasspathRoot cp, final Filter filter) {
    this.filter = filter;
    this.cp = cp;
  }

  public void parse(final AccessVisitor v) throws IOException {

    for (final ElementName each : this.cp.classNames()) {
      if (this.filter.include(each)) {
        parseClass(this.cp, v, each);
      }
    }

  }

  private void parseClass(final ClasspathRoot cp, final AccessVisitor dv,
      final ElementName each) throws IOException {
    final InputStream is = cp.getData(each);
    try {
      final ClassReader reader = new ClassReader(is);
      final DependencyClassVisitor cv = new DependencyClassVisitor(null,
          new FilteringDecorator(dv, this.filter));
      reader.accept(cv, 0);
    } finally {
      is.close();
    }

  }

}

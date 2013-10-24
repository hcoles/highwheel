package org.pitest.highwheel.bytecodeparser;


import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.classpath.ClassParser;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.ElementName;

public class ClassPathParser implements ClassParser {

  private final Filter        filter;
  private final NameTransformer nameTransformer;
  
  public ClassPathParser(final Filter filter) {
    this(filter, new CollapseInnerClassesNameTransformer());
  }

  public ClassPathParser(final Filter filter, final NameTransformer nameTransformer) {
    this.filter = filter;
    this.nameTransformer = nameTransformer;
  }

  public void parse(final ClasspathRoot classes, final AccessVisitor v) throws IOException {

    for (final ElementName each : classes.classNames()) {
      if (this.filter.include(each)) {
        parseClass(classes, v, each);
      }
    }

  }

  private void parseClass(final ClasspathRoot cp, final AccessVisitor dv,
      final ElementName each) throws IOException {
    final InputStream is = cp.getData(each);
    try {
      final ClassReader reader = new ClassReader(is);
      final DependencyClassVisitor cv = new DependencyClassVisitor(null,
          new FilteringDecorator(dv, this.filter), nameTransformer);
      reader.accept(cv, 0);
    } finally {
      is.close();
    }

  }

}

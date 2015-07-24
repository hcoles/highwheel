package org.pitest.highwheel.bytecodeparser;

import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;

/**
 * Passes calls through to wrapped child only if
 * they match the supplied filter
 */
class FilteringDecorator implements AccessVisitor {

  private final AccessVisitor child;
  private final Filter        filter;

  public FilteringDecorator(final AccessVisitor child, final Filter filter) {
    this.child = child;
    this.filter = filter;
  }

  @Override
  public void apply(final AccessPoint source, final AccessPoint dest,
      final AccessType type) {
    if (this.filter.include(dest.getElementName()) && this.filter.include(source.getElementName())) {
      this.child.apply(source, dest, type);
    }
  }

  @Override
  public void newNode(final ElementName clazz) {
    if (this.filter.include(clazz)) {
      this.child.newNode(clazz);
    }
  }

  @Override
  public void newEntryPoint(ElementName clazz) {
    if (this.filter.include(clazz)) {
      this.child.newEntryPoint(clazz);
    }
  }

  @Override
  public void newAccessPoint(AccessPoint ap) {
    this.child.newAccessPoint(ap);
  }

}

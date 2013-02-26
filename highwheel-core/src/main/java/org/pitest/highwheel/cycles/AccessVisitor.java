package org.pitest.highwheel.cycles;

import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;


public interface AccessVisitor {

  void apply(AccessPoint source, AccessPoint dest, AccessType type);

  void newNode(ElementName clazz);

  void newEntryPoint(ElementName clazz);

}

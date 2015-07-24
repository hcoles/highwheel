package org.pitest.highwheel.classpath;

import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;


public interface AccessVisitor {

  void apply(AccessPoint source, AccessPoint dest, AccessType type);

  void newNode(ElementName clazz);
  
  void newAccessPoint(AccessPoint ap);

  void newEntryPoint(ElementName clazz);

}

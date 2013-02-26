package org.pitest.highwheel.bytecodeparser;

import org.objectweb.asm.Type;
import org.pitest.highwheel.model.ElementName;

public class NameUtil {
  
  static ElementName getOutermostClassName(final String clazz) {
    if (clazz.contains("$")) {
      return ElementName.fromString(clazz.substring(0, clazz.indexOf('$')));
    }
    return ElementName.fromString(clazz);
  }
  
  static ElementName getElementNameForType(final org.objectweb.asm.Type type) {
    if (type.getSort() == Type.ARRAY) {
      return ElementName.fromString(type.getElementType().getClassName());
    }
    return ElementName.fromString(type.getClassName());
  }
}

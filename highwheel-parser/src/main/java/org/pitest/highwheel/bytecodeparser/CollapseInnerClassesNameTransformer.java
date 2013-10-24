package org.pitest.highwheel.bytecodeparser;

import org.pitest.highwheel.model.ElementName;

public class CollapseInnerClassesNameTransformer implements NameTransformer {

  public ElementName transform(String clazz) {
    if (clazz.contains("$")) {
      return ElementName.fromString(clazz.substring(0, clazz.indexOf('$')));
    }
    return ElementName.fromString(clazz);

  }

}

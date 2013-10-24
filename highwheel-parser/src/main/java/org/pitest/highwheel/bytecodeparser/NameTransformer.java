package org.pitest.highwheel.bytecodeparser;

import org.pitest.highwheel.model.ElementName;

public interface NameTransformer {
  
  public ElementName transform(String name);

}

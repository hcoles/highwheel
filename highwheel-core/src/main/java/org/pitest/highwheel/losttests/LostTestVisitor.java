package org.pitest.highwheel.losttests;

import org.pitest.highwheel.model.ElementName;

public interface LostTestVisitor {

  public void start();
  public void visitLostTest(ElementName test, ElementName testee);
  public void end();
}

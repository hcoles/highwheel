package org.pitest.highwheel.cycles;

import org.pitest.highwheel.model.ElementName;


public interface Filter {

  public boolean include(ElementName item);

}

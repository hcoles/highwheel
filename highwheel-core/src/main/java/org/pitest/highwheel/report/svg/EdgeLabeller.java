package org.pitest.highwheel.report.svg;

import org.apache.commons.collections15.Transformer;
import org.pitest.highwheel.model.Dependency;

public class EdgeLabeller implements Transformer<Dependency, String> {

  public String transform(final Dependency dep) {
    return "" + dep.getCount();
  }

}

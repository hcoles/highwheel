package org.pitest.highwheel.report.svg;

import org.apache.commons.collections15.Transformer;
import org.pitest.highwheel.model.ElementName;

public class VertexLabeller implements Transformer<ElementName, String> {

  public String transform(final ElementName arg0) {
    return arg0.getNameWithoutPackage().asJavaName();
  }

}

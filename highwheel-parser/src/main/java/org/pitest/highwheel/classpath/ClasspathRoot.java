package org.pitest.highwheel.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.pitest.highwheel.model.ElementName;

public interface ClasspathRoot {

  InputStream getData(ElementName name) throws IOException;

  Collection<ElementName> classNames();

  InputStream getResource(final String name) throws IOException;
}

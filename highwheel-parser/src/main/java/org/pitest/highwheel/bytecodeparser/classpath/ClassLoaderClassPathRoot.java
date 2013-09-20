package org.pitest.highwheel.bytecodeparser.classpath;

/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;

public class ClassLoaderClassPathRoot implements ClasspathRoot {

  private final ClassLoader loader;

  public ClassLoaderClassPathRoot(final ClassLoader loader) {
    this.loader = loader;
  }

  public Collection<ElementName> classNames() {
    return Collections.emptyList();
  }

  public InputStream getData(final ElementName name) throws IOException {
    return this.loader.getResourceAsStream(name.asInternalName() + ".class");
  }

  public InputStream getResource(final String name) throws IOException {
    return this.loader.getResourceAsStream(name);
  }

}

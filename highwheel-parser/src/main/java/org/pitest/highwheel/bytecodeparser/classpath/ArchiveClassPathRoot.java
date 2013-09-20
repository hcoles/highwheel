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
package org.pitest.highwheel.bytecodeparser.classpath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.util.StreamUtil;

/**
 * ClassPathRoot wrapping a jar or zip file
 */
public class ArchiveClassPathRoot implements ClasspathRoot {

  private final File file;

  public ArchiveClassPathRoot(final File file) {
    this.file = file;
  }

  public InputStream getData(final ElementName name) throws IOException {
    return getResource(name.asInternalName() + ".class");
  }

  public InputStream getResource(final String name) throws IOException {
    final ZipFile zip = getRoot();
    try {
      final ZipEntry entry = zip.getEntry(name);
      if (entry == null) {
        return null;
      }
      return StreamUtil.copyStream(zip.getInputStream(entry));
    } finally {
      zip.close();
    }
  }

  private void closeQuietly(final ZipFile zip) {
    try {
      zip.close();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return "ArchiveClassPathRoot [file=" + this.file.getName() + "]";
  }

  public Collection<ElementName> classNames() {
    final List<ElementName> names = new ArrayList<ElementName>();
    final ZipFile root = getRoot();
    try {
      final Enumeration<? extends ZipEntry> entries = root.entries();
      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
          names.add(stringToClassName(entry.getName()));
        }
      }
      return names;
    } finally {
      closeQuietly(root);
    }

  }

  private ElementName stringToClassName(final String name) {
    return ElementName.fromString(name.substring(0,
        (name.length() - ".class".length())));
  }

  private ZipFile getRoot() {
    try {
      return new ZipFile(this.file);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

}

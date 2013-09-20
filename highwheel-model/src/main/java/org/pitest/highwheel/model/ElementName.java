package org.pitest.highwheel.model;

import java.io.Serializable;

public final class ElementName implements Serializable, Comparable<ElementName> {

  private static final long serialVersionUID = 1L;

  private final String      name;

  public ElementName(final String name) {
    this(name, true);
  }

  public ElementName(final String name, final boolean intern) {
    if (intern) {
      this.name = removeSymbols(name).intern();
    } else {
      this.name = removeSymbols(name);
    }
  }

  private static String removeSymbols(final String name) {
    return name.replace('.', '/');
  }

  public ElementName(final Class<?> clazz) {
    this(clazz.getName());
  }

  public static ElementName fromString(final String clazz) {
    return new ElementName(clazz);
  }

  public static ElementName fromClass(final Class<?> clazz) {
    return new ElementName(clazz);
  }

  public String asJavaName() {
    return this.name.replace('/', '.');
  }

  public String asInternalName() {
    return this.name;
  }

  public ElementName getNameWithoutPackage() {
    final int lastSeperator = this.name.lastIndexOf('/');
    if (lastSeperator != -1) {
      return new ElementName(this.name.substring(lastSeperator + 1,
          this.name.length()));
    }
    return this;
  }

  public ElementName getParent() {
    final int lastSeperator = this.name.lastIndexOf('/');
    if (lastSeperator != -1) {
      return new ElementName(this.name.substring(0, lastSeperator));
    }
    return new ElementName("");
  }

  public ElementName withoutPrefixChars(final int prefixLength) {
    final String nameWithoutPackage = this.getNameWithoutPackage().asJavaName();
    return new ElementName(this.getParent().asJavaName()
        + "/"
        + nameWithoutPackage.substring(prefixLength,
            nameWithoutPackage.length()));
  }

  public ElementName withoutSuffixChars(final int suffixLength) {
    final String nameWithoutPacakge = this.getNameWithoutPackage().asJavaName();
    return new ElementName(this.getParent().asJavaName()
        + "/"
        + nameWithoutPacakge.substring(0, nameWithoutPacakge.length()
            - suffixLength));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ElementName other = (ElementName) obj;
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return asJavaName();
  }

  public int compareTo(final ElementName o) {
    return this.asJavaName().compareTo(o.asJavaName());
  }

}

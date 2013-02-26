package org.pitest.highwheel.model;

public final class AccessPoint {

  private final ElementName clazz;
  private final String      attribute;

  AccessPoint(final ElementName clazz, final String attribute) {
    this.clazz = clazz;
    this.attribute = attribute;
  }

  public static AccessPoint create(final ElementName clazz,
      final String attribute) {
    return new AccessPoint(clazz, attribute.replace('<', '(').replace('>', ')')
        .intern());
  }

  public static AccessPoint create(final ElementName clazz) {
    return new AccessPoint(clazz, null);
  }

  public AccessPoint methodAccess(final String name) {
    return create(this.clazz, name);
  }

  public ElementName getElementName() {
    return this.clazz;
  }

  public String getAttribute() {
    return this.attribute;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.attribute == null) ? 0 : this.attribute.hashCode());
    result = (prime * result)
        + ((this.clazz == null) ? 0 : this.clazz.hashCode());
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
    final AccessPoint other = (AccessPoint) obj;
    if (this.attribute == null) {
      if (other.attribute != null) {
        return false;
      }
    } else if (!this.attribute.equals(other.attribute)) {
      return false;
    }
    if (this.clazz == null) {
      if (other.clazz != null) {
        return false;
      }
    } else if (!this.clazz.equals(other.clazz)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    if (this.attribute != null) {
      return this.clazz.toString() + ":" + this.attribute;
    }
    return this.clazz.toString();

  }

}

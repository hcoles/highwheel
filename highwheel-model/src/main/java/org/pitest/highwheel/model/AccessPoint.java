package org.pitest.highwheel.model;

public final class AccessPoint {

  private final ElementName clazz;
  private final AccessPointName      attribute;

  AccessPoint(final ElementName clazz, final AccessPointName attribute) {
    this.clazz = clazz;
    this.attribute = attribute;
  }

  public static AccessPoint create(final ElementName clazz,
      final AccessPointName attribute) {
    return new AccessPoint(clazz, attribute);
  }

  public static AccessPoint create(final ElementName clazz) {
    return new AccessPoint(clazz, null);
  }

  public AccessPoint methodAccess(AccessPointName method) {
    return create(this.clazz, method);
  }

  public ElementName getElementName() {
    return this.clazz;
  }

  public AccessPointName getAttribute() {
    return this.attribute;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
    result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AccessPoint other = (AccessPoint) obj;
    if (attribute == null) {
      if (other.attribute != null)
        return false;
    } else if (!attribute.equals(other.attribute))
      return false;
    if (clazz == null) {
      if (other.clazz != null)
        return false;
    } else if (!clazz.equals(other.clazz))
      return false;
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

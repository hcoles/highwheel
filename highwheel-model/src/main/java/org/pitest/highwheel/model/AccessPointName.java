package org.pitest.highwheel.model;

public final class AccessPointName {
  
  private final String name;
  
  // We store the method/field descriptor so we can distinguish between different methods of
  // the same name. Not yet clear how this will be communicated externally
  private final String desc;
  
  AccessPointName(String name, String desc) {
    this.name = name;
    this.desc = desc;
  }

  public static AccessPointName create(String name, String desc) {
    return new AccessPointName(name.replace('<', '(').replace('>', ')').intern(),desc);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((desc == null) ? 0 : desc.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    AccessPointName other = (AccessPointName) obj;
    if (desc == null) {
      if (other.desc != null)
        return false;
    } else if (!desc.equals(other.desc))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    return name;
  }

}

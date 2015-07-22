package org.pitest.highwheel.model;

public final class Access {

  private final AccessType  type;
  private final AccessPoint source;
  private final AccessPoint dest;

  private Access(final AccessPoint source, final AccessPoint dest,
      final AccessType type) {
    this.type = type;
    this.dest = dest;
    this.source = source;
  }

  public static Access create(final AccessPoint source, final AccessPoint dest,
      final AccessType type) {
    return new Access(source, dest, type);
  }

  public int getStrength() {
    return this.type.getStrength();
  }

  public AccessType getType() {
    return this.type;
  }

  public AccessPoint getSource() {
    return this.source;
  }

  public AccessPoint getDest() {
    return this.dest;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dest == null) ? 0 : dest.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    Access other = (Access) obj;
    if (dest == null) {
      if (other.dest != null)
        return false;
    } else if (!dest.equals(other.dest))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    } else if (!source.equals(other.source))
      return false;
    if (type != other.type)
      return false;
    return true;
  }
  
  

}

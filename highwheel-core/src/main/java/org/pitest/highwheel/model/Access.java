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

}

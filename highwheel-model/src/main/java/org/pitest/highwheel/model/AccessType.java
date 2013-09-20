package org.pitest.highwheel.model;

public enum AccessType {
  USES(1), COMPOSED(2), INHERITANCE(4), IMPLEMENTS(4), ANNOTATED(2), SIGNATURE(
      3);

  private final int strength;

  AccessType(final int strength) {
    this.strength = strength;
  }

  public int getStrength() {
    return this.strength;
  }

}

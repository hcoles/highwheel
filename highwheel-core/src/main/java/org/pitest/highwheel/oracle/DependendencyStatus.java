package org.pitest.highwheel.oracle;

public enum DependendencyStatus {
  UNKNOWN(2), OK(3), FORBIDDEN(0), SUSPECT(1);

  private final int okIness;

  DependendencyStatus(final int okIness) {
    this.okIness = okIness;
  }

  public boolean lessDesirableThan(final DependendencyStatus s) {
    return this.okIness > s.okIness;
  }
}

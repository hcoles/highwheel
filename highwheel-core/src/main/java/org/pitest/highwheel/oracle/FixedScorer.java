package org.pitest.highwheel.oracle;

import org.pitest.highwheel.model.Access;

public class FixedScorer implements DependencyOracle {

  private final DependendencyStatus result;

  public FixedScorer(final DependendencyStatus result) {
    this.result = result;
  }

  public DependendencyStatus assess(final Access a) {
    return this.result;
  }

}

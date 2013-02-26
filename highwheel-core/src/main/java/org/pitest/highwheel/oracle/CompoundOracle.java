package org.pitest.highwheel.oracle;

import java.util.ArrayList;
import java.util.List;

import org.pitest.highwheel.model.Access;

public class CompoundOracle implements DependencyOracle {

  private final List<DependencyOracle> children = new ArrayList<DependencyOracle>();

  public CompoundOracle(final List<DependencyOracle> children) {
    this.children.addAll(children);
  }

  public DependendencyStatus assess(final Access a) {
    for (final DependencyOracle each : this.children) {
      final DependendencyStatus status = each.assess(a);
      if (status != DependendencyStatus.UNKNOWN) {
        return status;
      }
    }
    return DependendencyStatus.UNKNOWN;
  }

}

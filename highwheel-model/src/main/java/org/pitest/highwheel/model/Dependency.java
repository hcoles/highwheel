package org.pitest.highwheel.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Dependency {

  private final List<Access> consituents = new ArrayList<Access>(3);

  public int getCount() {
    return this.consituents.size();
  }

  public Collection<Access> consituents() {
    return this.consituents;
  }

  public int getStrength() {
    int s = 0;
    for (final Access each : this.consituents) {
      s = s + each.getStrength();
    }
    return s;
  }
  
  public void addDependency(final AccessPoint source, final AccessPoint dest,
      final AccessType type) {
    this.consituents.add(Access.create(source, dest, type));
  }
 
  @Override
  public String toString() {
    return "" + this.consituents;
  }

}

package org.pitest.highwheel.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class DependencyTest {
  
  @Test
  public void shouldTreatNonSameInstacesAsNonEqual() {
    Dependency a = new Dependency();
    Dependency b = new Dependency();
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));    
  }
  
  @Test
  public void shouldSumStrengthsOfConstituents() {
    Dependency testee = new Dependency();
    AccessPoint source = AccessPoint.create(ElementName.fromString("foo"));
    AccessPoint dest = AccessPoint.create(ElementName.fromString("foo"));
    testee.addDependency(source, dest, AccessType.COMPOSED);
    testee.addDependency(source, dest, AccessType.USES);
    assertEquals(AccessType.COMPOSED.getStrength() + AccessType.USES.getStrength(), testee.getStrength());
  }

}

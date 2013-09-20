package org.pitest.highwheel.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DependencyTest {

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

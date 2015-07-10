package org.pitest.highwheel.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class AccessPointTest {

  private final ElementName foo = ElementName.fromString("foo");

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(AccessPoint.class).verify();
  }

  @Test
  public void shouldCreateAnAccessPoint() {
    assertNotNull(AccessPoint.create(foo, AccessPointName.create("foo", "desc")));
  }

  @Test
  public void shouldCreateMethodAccessWithinSuppliedType() {
    AccessPoint testee = AccessPoint.create(foo);
    AccessPoint actual = testee.methodAccess(AccessPointName.create("bar", "()V"));
    assertEquals(foo, actual.getElementName());
    assertEquals(AccessPointName.create("bar", "()V"), actual.getAttribute());
  }

}

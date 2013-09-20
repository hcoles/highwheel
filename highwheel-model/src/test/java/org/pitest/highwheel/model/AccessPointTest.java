package org.pitest.highwheel.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AccessPointTest {
  
  private     final ElementName foo = ElementName.fromString("foo");

  @Test
  public void shouldCreateAnAccessPoint() {
    assertNotNull(AccessPoint.create(foo, "foo"));
  }
  
  @Test
  public void shouldReplaceAngleBracketsInAttributes() {
    assertEquals(AccessPoint.create(foo, "(init)"),
        AccessPoint.create(foo, "<init>"));
  }
  
  @Test
  public void shouldCreateMethodAccessWithinSuppliedType() {
    AccessPoint testee = AccessPoint.create(foo);
    AccessPoint actual = testee.methodAccess("bar");
    assertEquals(foo,actual.getElementName());
    assertEquals("bar",actual.getAttribute());
  }
  

}

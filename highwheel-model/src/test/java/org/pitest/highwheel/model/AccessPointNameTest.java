package org.pitest.highwheel.model;

import static org.junit.Assert.assertEquals;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class AccessPointNameTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(AccessPointName.class).verify();
  }
  
  @Test
  public void shouldReplaceAngleBracketsInAttributes() {
    assertEquals(AccessPointName.create("(init)", ""), AccessPointName.create("<init>", ""));
  }

}

package org.pitest.highwheel.model;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class AccessTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(Access.class).verify();
  }


}

package org.pitest.highwheel.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class ElementNameTest {
  
  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(ElementName.class).verify();
  }

  @Test
  public void shouldConvertJavaNamesToInternalNames() {
    final ElementName testee = new ElementName("com.foo.bar");
    assertEquals("com/foo/bar", testee.asInternalName());
  }

  @Test
  public void shouldConvertInternalNamesToJavaNames() {
    final ElementName testee = new ElementName("com/foo/bar");
    assertEquals("com.foo.bar", testee.asJavaName());
  }

  @Test
  public void shouldTreatSameClassNameAsEqual() {
    final ElementName left = new ElementName("com/foo/bar");
    final ElementName right = new ElementName("com.foo.bar");
    assertTrue(left.equals(right));
    assertTrue(right.equals(left));
  }

  @Test
  public void shouldDisplayJavaNameInToString() {
    final ElementName testee = new ElementName("com/foo/bar");
    assertEquals("com.foo.bar", testee.toString());
  }

  @Test
  public void getNameWithoutPackageShouldReturnNameOnlyWhenClassIsOuterClass() {
    assertEquals(new ElementName("String"),
        new ElementName(String.class).getNameWithoutPackage());
  }

  static class Foo {

  }

  @Test
  public void getNameWithoutPackageShouldReturnNameWhenClassIsInnerClass() {
    assertEquals(new ElementName("ElementNameTest$Foo"), new ElementName(
        Foo.class).getNameWithoutPackage());
  }

  @Test
  public void getNameWithoutPackageShouldReturnNameWhenClassInPackageDefault() {
    assertEquals(new ElementName("Foo"),
        new ElementName("Foo").getNameWithoutPackage());
  }

  @Test
  public void getPackageShouldReturnEmptyPackageWhenClassInPackageDefault() {
    assertEquals(new ElementName(""), new ElementName("Foo").getParent());
  }

  @Test
  public void getPackageShouldReturnPackageWhenClassWithinAPackage() {
    assertEquals(new ElementName("org.pitest.highwheel.model"),
        new ElementName(ElementNameTest.class).getParent());
  }

  @Test
  public void withoutSuffixCharsShouldReturnPacakgeAndClassWithoutSuffixChars() {
    assertEquals(new ElementName("com.example.Foo"), new ElementName(
        "com.example.FooTest").withoutSuffixChars(4));
  }

  @Test
  public void withoutPrefeixCharsShouldReturnPacakgeAndClassWithoutPrefixChars() {
    assertEquals(new ElementName("com.example.Foo"), new ElementName(
        "com.example.TestFoo").withoutPrefixChars(4));
  }

  @Test
  public void shouldSortByName() {
    final ElementName a = ElementName.fromString("a.a.c");
    final ElementName b = ElementName.fromString("a.b.c");
    final ElementName c = ElementName.fromString("b.a.c");

    final List<ElementName> actual = Arrays.asList(b, c, a);
    Collections.sort(actual);
    assertEquals(Arrays.asList(a, b, c), actual);
  }

  @Test
  public void shouldProduceSameHashCodeForSameClass() {
    assertEquals(ElementName.fromString("org/example/Foo").hashCode(),
        ElementName.fromString("org.example.Foo").hashCode());
  }

  @Test
  public void shouldProduceDifferentHashCodeForDifferentClasses() {
    assertFalse(ElementName.fromString("org/example/Foo").hashCode() == ElementName
        .fromString("org.example.Bar").hashCode());
  }

  @Test
  public void shouldTreatSameClassAsEqual() {
    assertEquals(ElementName.fromString("org/example/Foo"),
        ElementName.fromString("org.example.Foo"));
  }

  @Test
  public void shouldTreatDifferentClassesAsNotEqual() {
    assertFalse(ElementName.fromString("org/example/Foo").equals(
        ElementName.fromString("org.example.Bar")));
  }

}

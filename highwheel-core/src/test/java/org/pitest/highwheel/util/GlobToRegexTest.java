package org.pitest.highwheel.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class GlobToRegexTest {

  @Test
  public void shouldFindExactMatches() {
    final String value = "org.foo.foo";
    assertTrue(matches(value, value));
  }

  @Test
  public void shouldNotMatchNonMatchingStringWhenNoWildcardsPresent() {
    final String value = "org.foo.foo";
    final String glob = "org.foo";
    assertFalse(matches(glob, value));
  }

  @Test
  public void shouldMatchEverythingAfterAStar() {
    final String glob = "org.foo.*";
    assertTrue(matches(glob, "org.foo.foo"));
    assertTrue(matches(glob, "org.foo."));
    assertTrue(matches(glob, "org.foo.bar"));
  }

  @Test
  public void shouldNotMatchIfContentDiffersBeforeAStar() {
    final String glob = new String("org.foo.*");
    assertFalse(matches(glob, "org.fo"));
  }

  @Test
  public void shouldEscapeDotsInGeneratedRegex() {
    final String glob = new String("org.foo.bar");
    assertFalse(matches(glob, "orgafooabar"));
  }

  @Test
  public void shouldSupportQuestionMarkWildCard() {
    final String glob = new String("org?foo?bar");
    assertTrue(matches(glob, "org.foo.bar"));
    assertTrue(matches(glob, "orgafooabar"));
  }

  @Test
  public void shouldEscapeEscapesInGeneratedRegex() {
    final String glob = new String("org.\\bar");
    assertTrue(matches(glob, "org.\\bar"));
    assertFalse(matches(glob, "org.bar"));
  }

  @Test
  public void shouldEscapeDollarSign() {
    final String glob = new String("org$bar");
    assertTrue(matches(glob, "org$bar"));
  }
  
  @Test
  public void shouldSupportMultipleWildcards() {
    final String glob = new String("foo*bar*car");
    assertTrue(matches(glob, "foo!!!bar!!!car"));
    assertFalse(matches(glob, "foo!!!!!car"));
  }

  @Test
  public void shouldBeCaseSensitice() {
    final String glob = new String("foo*bar*car");
    assertTrue(matches(glob, "foo!!!bar!!!car"));
    assertFalse(matches(glob, "foo!!!Bar!!!car"));
  }

  private boolean matches(final String String, final String value) {
    return Pattern.matches(GlobToRegex.convertGlobToRegex(String), value);
  }
}

package org.pitest.highwheel.oracle;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.pitest.highwheel.model.Access;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;

public class SimpleFlatFileOracleParserTest {

  @Test
  public void shouldRankEverythingAsUnknownWhenGivenEmptyFile()
      throws IOException {
    final DependencyOracle oracle = make("");
    assertEquals(DependendencyStatus.UNKNOWN,
        oracle.assess(access("foo", "bar")));
  }

  @Test
  public void shouldRankAsUnknownWhenNoMatch() throws IOException {
    final DependencyOracle oracle = make("foo -> bar : OK");
    assertEquals(DependendencyStatus.UNKNOWN,
        oracle.assess(access("bar", "foo")));
  }

  @Test
  public void shouldRankAsUnknownWhenOnlySourceMatches() throws IOException {
    final DependencyOracle oracle = make("foo -> bar : OK");
    assertEquals(DependendencyStatus.UNKNOWN,
        oracle.assess(access("foo", "blah")));
  }

  @Test
  public void shouldRankAsUnknownWhenOnlyDestMatches() throws IOException {
    final DependencyOracle oracle = make("foo -> bar : OK");
    assertEquals(DependendencyStatus.UNKNOWN,
        oracle.assess(access("blah", "bar")));
  }

  @Test
  public void shouldRankAllowedDependenciesAsOk() throws IOException {
    final DependencyOracle oracle = make("foo -> bar : OK");
    assertEquals(DependendencyStatus.OK, oracle.assess(access("foo", "bar")));
  }

  @Test
  public void shoulDisallowedDependenciesAsForbidden() throws IOException {
    final DependencyOracle oracle = make("foo -> bar : FORBIDDEN");
    assertEquals(DependendencyStatus.FORBIDDEN,
        oracle.assess(access("foo", "bar")));
  }

  private Access access(final String from, final String to) {
    return Access.create(AccessPoint.create(ElementName.fromString(from)),
        AccessPoint.create(ElementName.fromString(to)), AccessType.COMPOSED);
  }

  private DependencyOracle make(final String data) throws IOException {
    final SimpleFlatFileOracleParser testee = new SimpleFlatFileOracleParser(
        makeInputStream(data));
    final DependencyOracle oracle = testee.parse();
    return oracle;
  }

  private static InputStream makeInputStream(final String s) {
    try {
      return new ByteArrayInputStream(s.getBytes("utf-8"));
    } catch (final UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

}

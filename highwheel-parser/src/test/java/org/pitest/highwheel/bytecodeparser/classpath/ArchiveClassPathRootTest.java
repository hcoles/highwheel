package org.pitest.highwheel.bytecodeparser.classpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.pitest.highwheel.bytecodeparser.classpath.ArchiveClassPathRoot;
import org.pitest.highwheel.model.ElementName;

public class ArchiveClassPathRootTest {

  private ArchiveClassPathRoot testee;

  @Before
  public void setup() throws Exception {
    // note mytests.jar is taken from
    // http://johanneslink.net/projects/cpsuite.jsp
    // assume GPL licence for this file. We do not link to any code within it
    // however
    this.testee = new ArchiveClassPathRoot(new File("mytests.jar"));
  }

  @Test
  public void classNamesShouldReturnAllClassNamesIArchive() {
    final Collection<ElementName> expected = Arrays.asList(
        ElementName.fromString("injar.p1.P1NoTest$InnerTest"),
        ElementName.fromString("injar.p1.P1NoTest"),
        ElementName.fromString("injar.p1.P1Test"),
        ElementName.fromString("injar.p2.P2Test"));
    assertEquals(expected, this.testee.classNames());
  }

  @Test
  public void getDataShouldReturnNullForUnknownClass() throws Exception {
    assertNull(this.testee.getData(ElementName.fromString("bar")));
  }

  @Test
  public void getDataShouldReturnInputStreamForAKnownClass() throws Exception {
    assertNotNull(this.testee
        .getData(ElementName.fromString("injar.p1.P1Test")));
  }

  @Test
  public void shouldReturnAReadableInputStream() {
    final byte b[] = new byte[100];
    try {
      final InputStream actual = this.testee.getData(ElementName
          .fromString("injar.p1.P1Test"));
      actual.read(b);
    } catch (final IOException ex) {
      fail();
    }
  }

}

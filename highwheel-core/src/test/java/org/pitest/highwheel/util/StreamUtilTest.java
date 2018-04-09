package org.pitest.highwheel.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

public class StreamUtilTest {

  @Test
  public void shouldReadToString() throws IOException {
    final InputStream is = new ByteArrayInputStream("foo".getBytes("UTF-8"));
    assertEquals("foo", StreamUtil.toString(is, "UTF-8"));
  }

  @Test
  public void shouldCopyStreamsToByteArrays() throws IOException {
    final byte[] expected = createByteArray();
    final ByteArrayInputStream bis = new ByteArrayInputStream(expected);
    final byte[] actual = StreamUtil.streamToByteArray(bis);
    assertArrayEquals(expected, actual);
  }
  
  @Test
  public void shouldCopyStreamsLargerThanBufferSize() throws IOException {
    final byte[] expected = new byte[(17 * 1024)];
    Arrays.fill(expected, (byte)2);
    final ByteArrayInputStream bis = new ByteArrayInputStream(expected);
    final byte[] actual = StreamUtil.streamToByteArray(bis);
    assertArrayEquals(expected, actual);
  }


  @Test
  public void shouldCopyContentsOfOneInputStreamToAnother() throws IOException {
    final byte[] expected = createByteArray();
    final InputStream actualStream = StreamUtil
        .copyStream(new ByteArrayInputStream(createByteArray()));
    final byte[] actualContents = StreamUtil.streamToByteArray(actualStream);
    assertArrayEquals(expected, actualContents);
  }

  private byte[] createByteArray() {
    final byte[] expected = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0xA };
    return expected;
  }
}

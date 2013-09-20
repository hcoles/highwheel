package org.pitest.highwheel.bytecodeparser.classpath;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.pitest.highwheel.bytecodeparser.classpath.DirectoryClassPathRoot;
import org.pitest.highwheel.model.ElementName;

public class DirectoryClassPathRootTest {

  private DirectoryClassPathRoot testee;

  @Test
  public void getDataShouldReturnNullForUnknownClass() throws Exception {
    this.testee = new DirectoryClassPathRoot(new File("foo"));
    assertNull(this.testee.getData(ElementName.fromString("bar")));
  }

  @Test
  public void shouldReturnClassNames() {
    final File root = new File("target/test-classes/"); // this is going to be
    // flakey as hell
    this.testee = new DirectoryClassPathRoot(root);
    assertTrue(this.testee.classNames().contains(
        ElementName.fromString(DirectoryClassPathRootTest.class.getName())));
  }

}

package org.pitest.highwheel.classpath;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.pitest.highwheel.model.ElementName;

public class ClassLoaderClassPathRootTest {

  private ClassLoaderClassPathRoot testee;

  @Before
  public void setup() {
    this.testee = new ClassLoaderClassPathRoot(Thread.currentThread()
        .getContextClassLoader());
  }

  @Test
  public void shouldReturnNoClassNames() {
    assertThat(this.testee.classNames()).isEmpty();
  }

  @Test
  public void shouldReturnsBytesForClassesVisibleToParentLoader()
      throws Exception {
    assertNotNull(this.testee.getData(ElementName
        .fromClass(ClassLoaderClassPathRootTest.class)));
    assertNotNull(Test.class.getName());
  }

  @Test
  public void testReturnsNullForClassesNotVisibleToParentLoader()
      throws Exception {
    assertNull(this.testee.getData(ElementName.fromString("FooFoo")));
  }

  @Test
  public void testReturnsNullForResourcesNotVisibleToParentLoader()
      throws Exception {
    assertNull(this.testee.getResource("not defined"));
  }

  @Test
  public void testReturnsInputStreamForResourcesVisibleToParentLoader()
      throws Exception {
    assertNotNull(this.testee.getResource("aresource.txt"));
  }

}

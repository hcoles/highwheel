package org.pitest.highwheel.bytecodeparser.classpath;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.bytecodeparser.classpath.DependencyClassPathRoot;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;

public class DependencyClassPathRootTest {

  private DependencyClassPathRoot testee;

  @Mock
  private ClasspathRoot           child;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new DependencyClassPathRoot(this.child);
  }

  @Test
  public void shouldNotReturnClassNames() {
    when(this.child.classNames()).thenReturn(
        Arrays.asList(ElementName.fromString("foo")));
    assertTrue(this.testee.classNames().isEmpty());
  }

  @Test
  public void shouldReturnResourcesFromChild() throws IOException {
    final InputStream is = Mockito.mock(InputStream.class);
    when(this.child.getResource("foo")).thenReturn(is);
    assertSame(is, this.testee.getResource("foo"));
  }

  @Test
  public void shouldReturnDataFromChild() throws IOException {
    final ElementName cn = ElementName.fromString("foo");
    final InputStream is = Mockito.mock(InputStream.class);
    when(this.child.getData(cn)).thenReturn(is);
    assertSame(is, this.testee.getData(cn));
  }

}

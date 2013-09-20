package org.pitest.highwheel.bytecodeparser;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.ElementName;

public class ClassPathParserTest {

  private ClassPathParser testee;

  @Mock
  private ClasspathRoot   cp;

  @Mock
  private Filter          filter;

  @Mock
  private AccessVisitor   v;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new ClassPathParser(this.filter);
  }

  @Test
  public void shouldNotParseClassesThatDoNotMatchFilter() throws IOException {
    final ElementName foo = ElementName.fromString("foo");
    when(this.cp.classNames()).thenReturn(Collections.singleton(foo));
    when(this.filter.include(foo)).thenReturn(false);
    testee.parse(cp,v);
    verify(cp,never()).getData(foo);
  }
  
  @Test
  public void shouldCloseClassInputStreams() throws IOException {
    final ElementName foo = ElementName.fromString("foo");
    when(this.cp.classNames()).thenReturn(Collections.singleton(foo));
    when(this.filter.include(foo)).thenReturn(true);
    final InputStream is = Mockito.mock(InputStream.class);
    when(this.cp.getData(foo)).thenReturn(is);
    when(is.read()).thenThrow(new IOException());
    try {
      this.testee.parse(cp,this.v);
    } catch (final IOException ex) {
      // expected
    }
    verify(is).close();

  }

}

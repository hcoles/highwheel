package org.pitest.highwheel.bytecodeparser;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessPointName;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;

public class FilteringDecoratorTest {

  private FilteringDecorator testee;

  @Mock
  private AccessVisitor      child;

  @Mock
  private Filter             filter;

  private final ElementName  fooElement = ElementName.fromString("foo");
  private final ElementName  barElement = ElementName.fromString("bar");

  private final AccessPoint  foo        =  AccessPoint.create(this.fooElement,
      AccessPointName.create("foo", "()V"));

  private final AccessPoint  bar        =  AccessPoint.create(this.barElement,
      AccessPointName.create("bar", "()V"));

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new FilteringDecorator(this.child, this.filter);
  }

  @Test
  public void shouldNotForwardCallWhenFilterDoesNotMatchSource() {
    when(this.filter.include(this.fooElement)).thenReturn(false);
    when(this.filter.include(this.barElement)).thenReturn(true);
    this.testee.apply(this.foo, this.bar, AccessType.COMPOSED);
    verify(this.child, never()).apply(this.foo, this.bar, AccessType.COMPOSED);
  }

  @Test
  public void shouldNotForwardCallWhenFilterDoesNotMatchDest() {
    when(this.filter.include(this.fooElement)).thenReturn(true);
    when(this.filter.include(this.barElement)).thenReturn(false);
    this.testee.apply(this.foo, this.bar, AccessType.COMPOSED);
    verify(this.child, never()).apply(this.foo, this.bar, AccessType.COMPOSED);
  }

  @Test
  public void shouldForwardCallWhenFilterMatchesSourceAndDest() {
    when(this.filter.include(this.fooElement)).thenReturn(true);
    when(this.filter.include(this.barElement)).thenReturn(true);
    this.testee.apply(this.foo, this.bar, AccessType.COMPOSED);
    verify(this.child).apply(this.foo, this.bar, AccessType.COMPOSED);
  }

  @Test
  public void shouldNotForwardNewNodeWhenFilterDoesNotMatch() {
    when(this.filter.include(this.fooElement)).thenReturn(false);
    this.testee.newNode(this.fooElement);
    verify(this.child, never()).newNode(this.fooElement);
  }

  @Test
  public void shouldForwardNewNodeWhenFilterMatches() {
    when(this.filter.include(this.fooElement)).thenReturn(true);
    this.testee.newNode(this.fooElement);
    verify(this.child).newNode(this.fooElement);
  }
}

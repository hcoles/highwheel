package org.pitest.highwheel.losttests;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;

public class LostTestAnalyserTest {
  
  private LostTestAnalyser testee = new LostTestAnalyser();
  
  @Mock
  private LostTestVisitor visitor;
  
  @Mock
  private ClasspathRoot mainRoot;
  
  @Mock
  private ClasspathRoot testRoot;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }
  
  @Test
  public void shouldReportLostTestsToVisitorWhenDetectedTesteeExists() {
    ElementName foo = ElementName.fromString("com.example.Foo");
    ElementName lostTest = ElementName.fromString("com.wrong.FooTest");
    when(mainRoot.classNames()).thenReturn(Arrays.asList(foo));
    when(testRoot.classNames()).thenReturn(Arrays.asList(lostTest));
    testee.analyse(mainRoot, testRoot, visitor);
    verify(visitor).visitLostTest(lostTest, foo);
  }
  
  @Test
  public void shouldNotReportLostTestsToVisitorWhenDetectedTesteeDoesNotExists() {
    ElementName foo = ElementName.fromString("com.example.Foo");
    ElementName lostTest = ElementName.fromString("com.wrong.FooTest");
    when(testRoot.classNames()).thenReturn(Arrays.asList(lostTest));
    testee.analyse(mainRoot, testRoot, visitor);
    verify(visitor, never()).visitLostTest(lostTest, foo);
  }
  
  @Test
  public void shouldCallVisitorStart() {
    testee.analyse(mainRoot, testRoot, visitor);
    verify(visitor).start();
  }
  
  @Test
  public void shouldCallVisitorEnd() {
    testee.analyse(mainRoot, testRoot, visitor);
    verify(visitor).end();
  }

}

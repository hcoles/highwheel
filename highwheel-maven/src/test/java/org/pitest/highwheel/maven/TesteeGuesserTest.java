package org.pitest.highwheel.maven;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;


public class TesteeGuesserTest {
	
	private final static ElementName FOO = ElementName.fromString("com.example.Foo");

	@Mock
	private ClasspathRoot root;
		
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldReturnNullWhenNoTesteeCanBeInferred() {
		assertEquals(null,makeTestee().guessTestee(ElementName.fromString("com.example.Foo")));
	}
	
	@Test
	public void shouldInferTesteeFromTestsWithNamesEndingInTest() {
		when(root.classNames()).thenReturn(Arrays.asList(FOO));
		assertEquals(FOO,makeTestee().guessTestee(ElementName.fromString("com.example.FooTest")));
	}
	
	@Test
	public void shouldInferTesteeFromTestsWithNamesBeginningWithTest() {
		when(root.classNames()).thenReturn(Arrays.asList(FOO));
		assertEquals(FOO,makeTestee().guessTestee(ElementName.fromString("com.example.TestFoo")));
	}
	
	@Test
	public void shouldSuggestMatchingAlternateClassWhenNoClassOfInferedNameExists() {
		when(root.classNames()).thenReturn(Arrays.asList(FOO));
		assertEquals(FOO,makeTestee().guessTestee(ElementName.fromString("com.different.package.TestFoo")));
	}
	
	private TesteeGuesser makeTestee() {
		return new TesteeGuesser(root);
	}
}

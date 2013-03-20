package org.pitest.highwheel.ant;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.model.ElementName;

public class ClassPathTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test
  public void shouldCreateRootsThatResolveClassesInSuppliedDirectory()
      throws IOException {
    this.tmpFolder.newFile("Foo.class");
    final List<ClasspathRoot> actual = ClassPath.createRoots(Arrays
        .asList(this.tmpFolder.getRoot()));
    assertEquals(1, actual.size());
    assertEquals(ElementName.fromString("Foo"), actual.get(0).classNames()
        .iterator().next());
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowExceptionWhenSuppliedDirectoryThatCannotBeRead() {
    ClassPath.createRoots(Arrays.asList(new File("doesNotExist")));
  }

}

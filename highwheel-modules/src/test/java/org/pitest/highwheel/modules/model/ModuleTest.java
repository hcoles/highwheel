package org.pitest.highwheel.modules.model;

import org.junit.Test;
import org.pitest.highwheel.model.ElementName;

import static org.fest.assertions.api.Assertions.assertThat;

public class ModuleTest {
    public static final String MODULE_NAME = "module name";
    public static final String GLOB = "org.pitest.foo*";
    private final Module testee = Module.make(MODULE_NAME, GLOB).get();

    @Test
    public void makeShouldFailIfRegexPassedIsInvalid() {
        assertThat(Module.make("a module", "[asdf").isPresent()).isFalse();
    }

    @Test
    public void makeShouldNotFailIfRegexPassedIsValid() {
        assertThat(Module.make("another module", ".*").isPresent()).isTrue();
    }

    @Test
    public void containsShouldBeTrueIfElementNameMatchesPattern() {
        assertThat(testee.contains(new ElementName("org.pitest.foo.Something"))).isTrue();
    }

    @Test
    public void containsShouldBeTrueOnMultiPatternModule() {
        Module testee = Module.make("a module with two patterns","a*","b*").get();
        assertThat(testee.contains(new ElementName("afoo"))).isTrue();
        assertThat(testee.contains(new ElementName("bfoo"))).isTrue();
    }

    @Test
    public void containsShouldFailIfAnyPatternFails() {
      assertThat(Module.make("a module", "valid","[invalid").isPresent()).isFalse();
    }

    @Test
    public void containsShouldBeFalseIfElementNameDoesNotMatchPattern() {
        assertThat(testee.contains(new ElementName("not.pitest.foo"))).isFalse();
    }

    @Test
    public void equalsShouldWorkOnModuleNameAndGlob() {
        assertThat(testee).isEqualTo(Module.make(MODULE_NAME,GLOB).get());
    }
}

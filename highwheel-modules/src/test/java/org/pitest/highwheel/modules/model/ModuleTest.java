package org.pitest.highwheel.modules.model;

import org.junit.Test;
import org.pitest.highwheel.model.ElementName;

import static org.fest.assertions.api.Assertions.assertThat;

public class ModuleTest {
    private final Module testee = Module.make("module name", "org.pitest.foo*").get();

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
    public void containsShouldBeFalseIfElementNameDoesNotMatchPattern() {
        assertThat(testee.contains(new ElementName("not.pitest.foo"))).isFalse();
    }
}

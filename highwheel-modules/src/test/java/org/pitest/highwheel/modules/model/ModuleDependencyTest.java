package org.pitest.highwheel.modules.model;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ModuleDependencyTest {

    private final Module moduleA = Module.make("module A", "module A").get();
    private final Module moduleB = Module.make("module B", "mpodule B").get();

    @Test
    public void getCountShouldReturnZeroOnNewDepedency() {
        assertThat(new ModuleDependency(moduleA,moduleB).getCount()).isEqualTo(0);
    }

    @Test
    public void getCountShouldIncreaseWhenCallingIncrementCount() {
        final ModuleDependency testee= new ModuleDependency(moduleA,moduleB);

        assertThat(testee.getCount()).isEqualTo(0);
        testee.incrementCount();
        assertThat(testee.getCount()).isEqualTo(1);
        assertThat(testee.getCount()).isEqualTo(1);
        testee.incrementCount();
        assertThat(testee.getCount()).isEqualTo(2);
    }
}

package org.pitest.highwheel.util.validation;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ObjectsTest {

    @Test(expected = AssertionError.class)
    public void requireNotNullShouldThrowAssertionErrorOnNull() {
        Objects.requireNotNull(null);
    }
    
    @Test
    public void requireNotNullShouldNotThrowAssertionErrorOnNotNull() {
        try {
            Objects.requireNotNull("");
        } catch(Throwable e) {
            fail();
        }
    }

    @Test
    public void nullShouldBeEqualToNull() {
        assertThat(Objects.equals(null,null)).isTrue();
    }

    @Test
    public void nullShouldNotBeEqualToDefined() {
        assertThat(Objects.equals(null, new Object())).isFalse();
    }

    @Test
    public void definedShouldNotBeEqualToNull() {
        assertThat(Objects.equals("", null)).isFalse();
    }

    @Test
    public void referenceShouldBeEqualToItself() {
        Object o = new Object();
        assertThat(Objects.equals(o,o)).isTrue();
    }

    private static class A {
        public String a;
        public String ignore;
        public A(String a, String ignore){
            this.a =a;
            this.ignore =ignore;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            A a1 = (A) o;

            return this.a.equals(a1.a);
        }
    }

    @Test
    public void definedEqualsShouldBeUsedIfAvailable() {
        A first = new A("IMPORTANT", "IMPORTANT"), second = new A("IMPORTANT", "NOT IMPORTANT");
        assertThat(Objects.equals(first,second)).isTrue();
    }
}

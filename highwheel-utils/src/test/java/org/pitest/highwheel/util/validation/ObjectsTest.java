package org.pitest.highwheel.util.validation;

import org.junit.Test;

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
}

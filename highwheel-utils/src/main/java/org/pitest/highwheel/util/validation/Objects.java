package org.pitest.highwheel.util.validation;

public final class Objects {

    public static void requireNotNull(Object o) {
        if(o == null) {
            throw new AssertionError("Value required not to be null");
        }
    }
}

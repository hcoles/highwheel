package org.pitest.highwheel.util.validation;

public final class Objects {

    public static void requireNotNull(Object o) {
        if(o == null) {
            throw new AssertionError("Value required not to be null");
        }
    }

    public static boolean equals(Object o1, Object o2) {
        if(o1 == o2)
            return true;
        if(o1 == null || o2 == null)
            return false;
        return o1.equals(o2);
    }
}

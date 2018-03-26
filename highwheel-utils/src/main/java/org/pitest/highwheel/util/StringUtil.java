package org.pitest.highwheel.util;

public final class StringUtil {

    public static <T> String join(String separator, Iterable<T> iterable) {
        final StringBuilder buff = new StringBuilder("");
        String sep = "";
        for(T item : iterable) {
            buff.append(sep).append(item.toString());
            sep = separator;
        }
        return buff.toString();
    }
}

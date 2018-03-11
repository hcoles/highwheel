package org.pitest.highwheel.modules.specification.tokens;

import org.pitest.highwheel.util.validation.Objects;

public class Token {
    public final Type type;
    public final String value;

    private Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public static Token makeOperator(String value) {
        return new Token(Type.OPERATOR,value);
    }

    public static Token makeGlob(String value) {
        return new Token(Type.GLOB,value);
    }

    public static Token makeNewLine() {
        return new Token(Type.NEWLINE,"");
    }

    public static Token makeSpacing() {
        return new Token(Type.SPACING,"");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        return Objects.equals(type,token.type) && Objects.equals(value,token.value);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}

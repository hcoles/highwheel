package org.pitest.highwheel.modules.specification.tokens;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.pitest.highwheel.modules.specification.tokens.Token.*;
public class TokenTest {

    @Test
    public void makeGlobShouldCreateATokenWithTypeGlob() {
        final Token testee = makeGlob("name");
        assertThat(testee.type).isEqualTo(Type.GLOB);
        assertThat(testee.value).isEqualTo("name");
    }

    @Test
    public void makeOperatorShouldCreateATokenWithTypeOperator() {
        final Token testee = makeOperator("operator");
        assertThat(testee.type).isEqualTo(Type.OPERATOR);
        assertThat(testee.value).isEqualTo("operator");
    }

    @Test
    public void makeNewLineShouldCreateATokenWithTypeNewLine() {
        final Token testee = makeNewLine();
        assertThat(testee.type).isEqualTo(Type.NEWLINE);
    }

    @Test
    public void makeSpacingShouldCreateATokenWithTypeSpacing() {
        final Token testee = makeSpacing();
        assertThat(testee.type).isEqualTo(Type.SPACING);
    }

    @Test
    public void equalsShouldReturnFalseOnDifferentType() {
        assertThat(makeGlob("name")).isNotEqualTo(makeOperator("name"));
    }

    @Test
    public void equalsShouldReturnFalseOnDifferentValue() {
        assertThat(makeGlob("name")).isNotEqualTo(makeGlob("other name"));
    }

    @Test
    public void equalsShouldReturnTrueOnSameTypeAndValue() {
        assertThat(makeGlob("name")).isEqualTo(makeGlob("name"));
    }
}

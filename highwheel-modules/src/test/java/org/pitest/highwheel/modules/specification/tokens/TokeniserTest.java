package org.pitest.highwheel.modules.specification.tokens;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.pitest.highwheel.modules.specification.tokens.Token.*;

public class TokeniserTest {

    private final Tokeniser testee = new Tokeniser();
    @Test
    public void shouldRecogniseGlobs() {
        assertThat(testee.tokenise("asdfGFDE_123?*.".toCharArray())).containsExactly(makeGlob("asdfGFDE_123?*."));
    }

    @Test
    public void shouldRecogniseEqualDashSlashGreaterThanBracketsAsOperators() {
        assertThat(testee.tokenise("=-/>[]".toCharArray())).containsExactly(makeOperator("=-/>[]"));
    }

    @Test
    public void shouldRecogniseTabsAsSpacing() {
        assertThat(testee.tokenise("\t\t".toCharArray())).containsExactly(makeSpacing());
    }

    @Test
    public void shouldRecogniseSpacesAsSpacing() {
        assertThat(testee.tokenise("  ".toCharArray())).containsExactly(makeSpacing());
    }

    @Test
    public void shouldRecogniseNewLineAsNewLine() {
        assertThat(testee.tokenise("\n".toCharArray())).containsExactly(makeNewLine());
    }

    @Test
    public void shouldTokeniseMultipleElementsAsDifferentTokens() {
        assertThat(testee.tokenise("asdf _df223??* [[ \n\t -->>>  ".toCharArray())).containsExactly(
                makeGlob("asdf"),
                makeSpacing(),
                makeGlob("_df223??*"),
                makeSpacing(),
                makeOperator("[["),
                makeSpacing(),
                makeNewLine(),
                makeSpacing(),
                makeOperator("-->>>"),
                makeSpacing());
    }

    @Test(expected = UnrecognisedCharacterExcpetion.class)
    public void shouldThrowExcpetionOnUnrecognisedCharacter() {
        testee.tokenise("asdfsadf    sdfwer %".toCharArray());
    }
}

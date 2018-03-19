package org.pitest.highwheel.modules.specification.parsers;


import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class TerminalParserTest {

    private final TerminalParser testee = new TerminalParser();
    private final Parser<?> parser = testee.tokeniser();
    @Test
    public void operatorsShouldBeTokenised() {
        parser.parse("=");
        parser.parse("->");
        parser.parse("-/->");
        parser.parse("\n");
        parser.parse(":");
        parser.parse(",");
    }

    @Test
    public void shouldNotTokeniseOtherSpecialCharacters() {
        final String[] otherSpecial = new String[] {">", "<->", ".", ";"};
        for(String other: otherSpecial) {
            boolean exceptionThrown = false;
            try {
                parser.parse(other);
            } catch(Exception e) {
                exceptionThrown = true;
            }
            assertThat(exceptionThrown).isTrue().describedAs(other + " was parsed but it shouldn't have been");
        }
    }

    @Test
    public void keywordsShouldBeTokenised() {
        parser.parse("modules");
        parser.parse("rules");
    }

    @Test
    public void identifiersShouldBeTokenised() {
        parser.parse("foobar");
        parser.parse("_barfoo_");
        parser.parse("A12SDss__sdf");
    }

    @Test
    public void doubleQuotedStringsShouldBeTokenised() {
        parser.parse("\"asdf\"");
        parser.parse("\"something that would not be normally parsed ---a--cc''..s12312312\\\"\"");
    }

    @Test
    public void equalsShouldParseEqual() {
        assertParse(testee.equals(),"=");
    }

    @Test
    public void commaShouldParseComma() {
        assertParse(testee.comma(),",");
    }

    @Test
    public void arrowShouldParseArrow() {
        assertParse(testee.arrow(),"->");
    }

    @Test
    public void notArrowShouldParseNotArrow() {
        assertParse(testee.notArrow(),"-/->");
    }

    @Test
    public void definedAsShouldParseColumn() {
        assertParse(testee.definedAs(),":");
    }

    @Test
    public void modulesPreambleShouldParseModulesKeyword() {
        assertParse(testee.modulesPreamble(),"modules");
    }

    @Test
    public void rulesPreambleShouldParseRulesKeyword() {
        assertParse(testee.rulesPreamble(),"rules");
    }

    @Test
    public void newLineShouldParseNewLine() {
        assertParse(testee.newLine(),"\n");
    }

    @Test
    public void moduleNameShouldParseIdentifiers() {
        assertParse(testee.moduleName(),"_an_identifier");
    }

    @Test
    public void moduleRegexShouldParseDoubleQuotedStringLiteral() {
        assertParse(testee.moduleRegex(),"\"asdfasdf121123  sdfwe{{\"");
    }

    @Test(expected = RuntimeException.class)
    public void moduleRegexShouldFailOnNotTerminatedDoubleQuotedStringLiteral() {
        assertParse(testee.moduleRegex(),"\"asdfasdf121123  sdfwe{{");
    }

    public void assertParse(Parser<?> p, String source) {
        p.from(parser, Parsers.EOF.skipMany()).parse(source);
    }
}

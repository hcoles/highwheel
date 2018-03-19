package org.pitest.highwheel.modules.specification.parsers;


import org.jparsec.*;

final class TerminalParser {
    private final String[] operators = {
            "=","\n",":","->","-/->",","
    };

    private final Terminals terminals = Terminals.operators(operators).words(Scanners.IDENTIFIER).keywords("modules","rules").build();

    private final Parser<?> tokeniser = Parsers.<Object>or(terminals.tokenizer(),Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER);

    private Parser<Token> term(String name) {
        return terminals.token(name);
    }

    private final Parser<Token> equals = term("=");
    public Parser<Token> equals() {
        return this.equals;
    }

    private final Parser<Token> comma = term(",");
    public Parser<Token> comma() {return comma;}

    private final Parser<Token> arrow = term("->");
    public Parser<Token> arrow(){
        return this.arrow;
    }

    private final Parser<Token> notArrow = term("-/->");
    public Parser<Token> notArrow() {
        return this.notArrow;
    }

    private final Parser<Token> definedAs = term(":");
    public Parser<Token> definedAs() {
        return this.definedAs;
    }

    private final Parser<Token> newLine = term("\n");
    public Parser<Token> newLine() {
        return this.newLine;
    }

    private final Parser<Token> modulesPreamble = term("modules");
    public Parser<Token> modulesPreamble() {
        return this.modulesPreamble;
    }

    private final Parser<Token> rulesPreamble = term("rules");
    public Parser<Token> rulesPreamble() {
        return this.rulesPreamble;
    }

    private final Parser<String> moduleName = Terminals.Identifier.PARSER;
    public Parser<String> moduleName() {
        return this.moduleName;
    }

    private final Parser<String> moduleRegex = Terminals.StringLiteral.PARSER;
    public Parser<String> moduleRegex() {
        return this.moduleRegex;
    }

    public TerminalParser() {}

    public Parser<?> tokeniser() {
        return tokeniser;
    }
}

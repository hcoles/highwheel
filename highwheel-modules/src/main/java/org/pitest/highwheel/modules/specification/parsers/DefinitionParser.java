package org.pitest.highwheel.modules.specification.parsers;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.functors.Map3;
import org.codehaus.jparsec.functors.Map4;
import org.pitest.highwheel.modules.specification.SyntaxTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class DefinitionParser {

    final TerminalParser tp = new TerminalParser();

    final Parser<SyntaxTree.ModuleDefinition> moduleDefinitionParser =
            Parsers.sequence(tp.moduleName(), tp.equals(), tp.moduleRegex(), tp.newLine(), new Map4<String, Token, String, Token, SyntaxTree.ModuleDefinition>() {
                @Override
                public SyntaxTree.ModuleDefinition map(String s, Token token, String s2, Token token2) {
                    return new SyntaxTree.ModuleDefinition(s,s2);
                }
            });

    final Parser<SyntaxTree.ChainDependencyRule> chainDependencyRuleParser =
            Parsers.sequence(tp.moduleName(), Parsers.sequence(tp.arrow(), tp.moduleName()).many1(), tp.newLine(), new Map3<String, List<String>,Token, SyntaxTree.ChainDependencyRule>() {
                @Override
                public SyntaxTree.ChainDependencyRule map(String s, List<String> strings, Token t) {
                    final List<String> result = new ArrayList<String>(strings.size() + 1);
                    result.add(s);
                    result.addAll(strings);
                    return new SyntaxTree.ChainDependencyRule(result);
                }
            });

    final Parser<SyntaxTree.NoDependentRule> noDependecyRuleParser =
            Parsers.sequence(tp.moduleName(), tp.notArrow(), tp.moduleName(), tp.newLine(), new Map4<String, Token, String, Token, SyntaxTree.NoDependentRule>() {
                @Override
                public SyntaxTree.NoDependentRule map(String s, Token token, String s2, Token token2) {
                    return new SyntaxTree.NoDependentRule(s,s2);
                }
            });

    private final Parser<SyntaxTree.Rule> anyRuleParser = Parsers.or(chainDependencyRuleParser,noDependecyRuleParser);

    final Parser<List<SyntaxTree.Rule>> rulesParser = Parsers.sequence(anyRuleParser, tp.newLine().many(), new Map2<SyntaxTree.Rule, List<Token>, SyntaxTree.Rule>() {
        @Override
        public SyntaxTree.Rule map(SyntaxTree.Rule rule, List<Token> tokens) {
            return rule;
        }
    }).many();

    final Parser<List<SyntaxTree.ModuleDefinition>> moduleDefinitions = Parsers.sequence(moduleDefinitionParser, tp.newLine().many(), new Map2<SyntaxTree.ModuleDefinition, List<Token>, SyntaxTree.ModuleDefinition>() {
        @Override
        public SyntaxTree.ModuleDefinition map(SyntaxTree.ModuleDefinition moduleDefinition, List<Token> tokens) {
            return moduleDefinition;
        }
    }).many();

    final Parser<Void> modulesPreamble = Parsers.sequence(tp.modulesPreamble(), tp.definedAs(), tp.newLine().many(), new Map3<Token, Token, List<Token>, Void>() {
        @Override
        public Void map(Token token, Token token2, List<Token> d) {
            return null;
        }
    });

    final Parser<Void> rulesPreamble = Parsers.sequence(tp.rulesPreamble(), tp.definedAs(), tp.newLine().many(), new Map3<Token, Token, List<Token>, Void>() {
        @Override
        public Void map(Token token, Token token2, List<Token> d) {
            return null;
        }
    });

    final Parser<List<SyntaxTree.ModuleDefinition>> modulesSection = Parsers.sequence(modulesPreamble,moduleDefinitions);

    final Parser<List<SyntaxTree.Rule>> rulesSection = Parsers.sequence(rulesPreamble,rulesParser);

    final Parser<SyntaxTree.Definition> grammar = Parsers.sequence(modulesSection, rulesSection, new Map2<List<SyntaxTree.ModuleDefinition>, List<SyntaxTree.Rule>, SyntaxTree.Definition>() {
        @Override
        public SyntaxTree.Definition map(List<SyntaxTree.ModuleDefinition> moduleDefinitions, List<SyntaxTree.Rule> rules) {
            return new SyntaxTree.Definition(moduleDefinitions,rules);
        }
    });

    private static final Parser<Void> ignore = Scanners.among(" \t");

    public SyntaxTree.Definition parse(Readable readable) {
        try {
            return grammar.from(tp.tokeniser(), ignore.skipMany()).parse(readable);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}

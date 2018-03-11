package org.pitest.highwheel.modules.specification.tokens;

import org.pitest.highwheel.util.base.Optional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.pitest.highwheel.modules.specification.tokens.Token.*;

public class Tokeniser {

    private abstract class CharacterFamily {

        public int getLastInFamilyFrom(char[] characters, int start) {
            int index = start;
            while(index < characters.length && characterInFamily(characters[index])) {
                ++index;
            }
            return index;
        }

        public abstract boolean characterInFamily(char c);

        public abstract Token makeToken(char[] characters, int from, int to);
    }

    private class GlobFamily extends CharacterFamily {
        @Override
        public boolean characterInFamily(char c) {
            return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' ||
                    c >= '0' && c <= '9' || c == '.' || c == '?' ||
                    c == '*' || c == '_';
        }

        @Override
        public Token makeToken(char[] characters, int from, int to) {
            return makeGlob(String.copyValueOf(characters,from,to - from));
        }


    }

    private class OperatorFamily extends  CharacterFamily {
        @Override
        public boolean characterInFamily(char c) {
            return c == '-' || c == '>' || c == '[' || c == ']' ||
                    c == '=' || c == '/';
        }

        @Override
        public Token makeToken(char[] characters, int from, int to) {
            return makeOperator(String.copyValueOf(characters,from,to - from));
        }
    }

    private class SpacingFamily extends  CharacterFamily {

        @Override
        public boolean characterInFamily(char c) {
            return c == ' ' || c == '\t';
        }

        @Override
        public Token makeToken(char[] characters, int from, int to) {
            return makeSpacing();
        }
    }

    private class NewLineFamily extends CharacterFamily {

        @Override
        public boolean characterInFamily(char c) {
            return c == '\n';
        }

        @Override
        public Token makeToken(char[] characters, int from, int to) {
            return makeNewLine();
        }
    }

    private List<CharacterFamily> allowedCharacterFamilies = Arrays.asList(
            new GlobFamily(),
            new OperatorFamily(),
            new SpacingFamily(),
            new NewLineFamily()
    );

    public List<Token> tokenise(final char[] charArray) {
        final List<Token> result = new ArrayList<Token>();
        int i = 0;
        while(i < charArray.length) {
            final char currentChar = charArray[i];
            final Optional<CharacterFamily> characterFamilyOptional = isCharacterAllowed(currentChar);
            if(characterFamilyOptional.isPresent()) {
                final CharacterFamily family = characterFamilyOptional.get();
                int end = family.getLastInFamilyFrom(charArray,i);
                result.add(family.makeToken(charArray,i, end));
                i = end;
            } else {
                throw new UnrecognisedCharacterExcpetion(String.format("'%s' is not a recognised character",currentChar));
            }
        }
        return result;
    }
    private Optional<CharacterFamily> isCharacterAllowed(char c) {
        for(CharacterFamily family: allowedCharacterFamilies) {
            if(family.characterInFamily(c)){
                return Optional.of(family);
            }
        }
        return Optional.empty();
    }
}

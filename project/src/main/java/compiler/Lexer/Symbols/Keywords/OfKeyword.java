package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class OfKeyword extends KeywordType {
    public static final String OF_KEYWORD = "OF_KEYWORD";

    public OfKeyword() {
        super(OF_KEYWORD, "of");
    }
}
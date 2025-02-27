package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class FalseKeyword extends KeywordType {
    public static final String FALSE_KEYWORD = "FALSE_KEYWORD";

    public FalseKeyword() {
        super(FALSE_KEYWORD, "false");
    }
}
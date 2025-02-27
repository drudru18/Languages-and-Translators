package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class FreeKeyword extends KeywordType {
    public static final String FREE_KEYWORD = "FREE_KEYWORD";

    public FreeKeyword() {
        super(FREE_KEYWORD, "free");
    }
}
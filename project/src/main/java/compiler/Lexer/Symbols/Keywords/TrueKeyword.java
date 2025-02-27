package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class TrueKeyword extends KeywordType {
    public static final String TRUE_KEYWORD = "TRUE_KEYWORD";

    public TrueKeyword() {
        super(TRUE_KEYWORD, "true");
    }
}
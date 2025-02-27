package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class ReturnKeyword extends KeywordType {
    public static final String RETURN_KEYWORD = "RETURN_KEYWORD";

    public ReturnKeyword() {
        super(RETURN_KEYWORD, "return");
    }
}
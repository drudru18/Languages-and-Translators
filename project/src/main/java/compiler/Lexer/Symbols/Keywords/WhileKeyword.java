package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class WhileKeyword extends KeywordType {
    public static final String WHILE_KEYWORD = "WHILE_KEYWORD";

    public WhileKeyword() {
        super(WHILE_KEYWORD, "while");
    }
}
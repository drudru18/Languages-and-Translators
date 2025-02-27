package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class ElseKeyword extends KeywordType {
    public static final String ELSE_KEYWORD = "ELSE_KEYWORD";

    public ElseKeyword() {
        super(ELSE_KEYWORD, "else");
    }
}
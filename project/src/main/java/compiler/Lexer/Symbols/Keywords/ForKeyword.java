package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class ForKeyword extends KeywordType {
    public static final String FOR_KEYWORD = "FOR_KEYWORD";

    public ForKeyword() {
        super(FOR_KEYWORD, "for");
    }
}
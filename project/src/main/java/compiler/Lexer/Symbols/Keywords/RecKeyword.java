package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class RecKeyword extends KeywordType {
    public static final String REC_KEYWORD = "REC_KEYWORD";

    public RecKeyword() {
        super(REC_KEYWORD, "rec");
    }
}
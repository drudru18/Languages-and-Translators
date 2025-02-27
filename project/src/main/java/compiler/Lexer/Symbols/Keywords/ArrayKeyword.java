package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class ArrayKeyword extends KeywordType {
    public static final String ARRAY_KEYWORD = "ARRAY_KEYWORD";

    public ArrayKeyword() {
        super(ARRAY_KEYWORD, "array");
    }
}
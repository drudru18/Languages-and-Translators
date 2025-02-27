package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class FunKeyword extends KeywordType {
    public static final String FUN_KEYWORD = "FUN_KEYWORD";

    public FunKeyword() {
        super(FUN_KEYWORD, "fun");
    }
}
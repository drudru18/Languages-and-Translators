package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class FinalKeyword extends KeywordType {
    public static final String FINAL_KEYWORD = "FINAL_KEYWORD";

    public FinalKeyword() {
        super(FINAL_KEYWORD, "final");
    }
}
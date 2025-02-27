package compiler.Lexer.Symbols.Keywords;

import compiler.Lexer.Symbols.KeywordType;

public class IfKeyword extends KeywordType {
    public static final String IF_KEYWORD = "IF_KEYWORD";

    public IfKeyword() {
        super(IF_KEYWORD, "if");
    }
}
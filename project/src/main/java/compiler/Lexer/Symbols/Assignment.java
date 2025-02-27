package compiler.Lexer.Symbols;

import compiler.Lexer.Symbol;

public class Assignment extends Symbol {
    public static final String ASSIGNMENT = "ASSIGNMENT";

    public Assignment() {
        super(ASSIGNMENT, "=");
    }
}
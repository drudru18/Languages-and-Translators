package compiler.Lexer.Symbols;

import compiler.Lexer.Symbol;

public class Not extends Symbol {
    public static final String NOT = "NOT";

    public Not() {
        super(NOT, "!");
    }
}
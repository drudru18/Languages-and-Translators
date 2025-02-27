package compiler.Lexer.Symbols;

import compiler.Lexer.Symbol;

public class IdentifierType extends Symbol {
    public static final String IDENTIFIER = "IDENTIFIER";

    public IdentifierType(String value) {
        super(IDENTIFIER, value);
    }
}

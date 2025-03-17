package compiler.Lexer.Symbols;

import compiler.Lexer.Symbol;

public class RecordIdentifierType extends Symbol {
    public static final String RECORD_IDENTIFIER = "RECORD_IDENTIFIER";

    public RecordIdentifierType(String value) {
        super(RECORD_IDENTIFIER, value);
    }
}

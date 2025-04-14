package compiler.Lexer.Symbols;

import compiler.Lexer.Symbol;

public class StringValue extends Symbol {
    public static final String STRING_VALUE = "STRING_VALUE";

    public StringValue(String value) {
        super(STRING_VALUE, value);
    }
}
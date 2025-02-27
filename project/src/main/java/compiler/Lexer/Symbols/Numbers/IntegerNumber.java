package compiler.Lexer.Symbols.Numbers;

import compiler.Lexer.Symbols.NumberValue;

public class IntegerNumber extends NumberValue {
    public static final String INTEGER_NUMBER = "INTEGER_NUMBER";

    public IntegerNumber(String value) {super(INTEGER_NUMBER, value);}
}

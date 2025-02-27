package compiler.Lexer.Symbols.Numbers;

import compiler.Lexer.Symbols.NumberValue;

public class FloatNumber extends NumberValue {
    public static final String FLOAT_NUMBER = "FLOAT_NUMBER";

    public FloatNumber(String value) {super(FLOAT_NUMBER, value);}
}

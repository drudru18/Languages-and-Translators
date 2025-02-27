package compiler.Lexer.Symbols;

import compiler.Lexer.Symbol;

public class EndOfInputType extends Symbol {
    public static final String END_OF_INPUT = "END_OF_INPUT";
    public static final String END_OF_INPUT_value = "\0";

    public EndOfInputType() {
        super(END_OF_INPUT, END_OF_INPUT_value);
    }
}
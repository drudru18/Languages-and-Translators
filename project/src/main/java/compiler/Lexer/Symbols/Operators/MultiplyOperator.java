package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class MultiplyOperator extends OperatorType {
    public static final String MULTIPLY_OPERATOR = "MULTIPLY_OPERATOR";

    public MultiplyOperator() {super(MULTIPLY_OPERATOR, "*");}
}
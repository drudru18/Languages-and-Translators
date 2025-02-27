package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class MinusOperator extends OperatorType {
    public static final String MINUS_OPERATOR = "MINUS_OPERATOR";

    public MinusOperator() {super(MINUS_OPERATOR, "-");}
}
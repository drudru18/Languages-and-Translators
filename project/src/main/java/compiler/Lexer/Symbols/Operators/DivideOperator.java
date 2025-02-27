package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class DivideOperator extends OperatorType {
    public static final String DIVIDE_OPERATOR = "DIVIDE_OPERATOR";

    public DivideOperator() {super(DIVIDE_OPERATOR, "/");}
}
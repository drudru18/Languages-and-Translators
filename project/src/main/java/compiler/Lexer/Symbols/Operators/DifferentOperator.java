package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class DifferentOperator extends OperatorType {
    public static final String DIFFERENT_OPERATOR = "DIFFERENT_OPERATOR";

    public DifferentOperator() {super(DIFFERENT_OPERATOR, "!=");}
}
package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class GreaterOrEqualsOperator extends OperatorType {
    public static final String GREATER_OR_EQUALS_OPERATOR = "GREATER_OR_EQUALS_OPERATOR";

    public GreaterOrEqualsOperator() {super(GREATER_OR_EQUALS_OPERATOR, ">=");}
}
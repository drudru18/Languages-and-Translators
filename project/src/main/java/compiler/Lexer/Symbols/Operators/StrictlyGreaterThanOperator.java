package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class StrictlyGreaterThanOperator extends OperatorType {
    public static final String STRICTLY_GREATER_THAN_OPERATOR = "STRICTLY_GREATER_THAN_OPERATOR";

    public StrictlyGreaterThanOperator() {super(STRICTLY_GREATER_THAN_OPERATOR, ">");}
}
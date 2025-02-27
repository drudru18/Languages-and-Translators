package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class LowerOrEqualsOperator extends OperatorType {
    public static final String LOWER_OR_EQUALS_OPERATOR = "LOWER_OR_EQUALS_OPERATOR";

    public LowerOrEqualsOperator() {super(LOWER_OR_EQUALS_OPERATOR, "<=");}
}
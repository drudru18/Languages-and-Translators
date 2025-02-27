package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class EqualsOperator extends OperatorType {
    public static final String EQUALS_OPERATOR = "EQUALS_OPERATOR";

    public EqualsOperator() {super(EQUALS_OPERATOR, "==");}
}
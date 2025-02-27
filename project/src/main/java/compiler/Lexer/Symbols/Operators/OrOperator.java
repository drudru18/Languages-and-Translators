package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class OrOperator extends OperatorType {
    public static final String OR_OPERATOR = "OR_OPERATOR";

    public OrOperator() {super(OR_OPERATOR, "||");}
}
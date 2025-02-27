package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class PlusOperator extends OperatorType {
    public static final String PLUS_OPERATOR = "PLUS_OPERATOR";

    public PlusOperator() {super(PLUS_OPERATOR, "+");}
}
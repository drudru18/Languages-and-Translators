package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class AndOperator extends OperatorType {
    public static final String AND_OPERATOR = "AND_OPERATOR";

    public AndOperator() {super(AND_OPERATOR, "&&");}
}
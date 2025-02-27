package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class StrictlyLowerThanOperator extends OperatorType {
    public static final String STRICTLY_LOWER_THAN_OPERATOR = "STRICTLY_LOWER_THAN_OPERATOR";

    public StrictlyLowerThanOperator() {super(STRICTLY_LOWER_THAN_OPERATOR, "<");}
}
package compiler.Lexer.Symbols.Operators;

import compiler.Lexer.Symbols.OperatorType;

public class ModuloOperator extends OperatorType {
    public static final String MODULO_OPERATOR = "MODULO_OPERATOR";

    public ModuloOperator() {super(MODULO_OPERATOR, "%");}
}
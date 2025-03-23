package compiler.Lexer.Symbols.Keywords.Types;

import compiler.Lexer.Symbols.Keywords.BasicType;

public class IntType extends BasicType {
    public static final String INT_TYPE = "INT_TYPE";

    public IntType() {
        super(INT_TYPE, "int");
    }
}

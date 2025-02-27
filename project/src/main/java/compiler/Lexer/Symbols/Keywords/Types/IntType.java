package compiler.Lexer.Symbols.Keywords.Types;

import compiler.Lexer.Symbols.Keywords.TypeType;

public class IntType extends TypeType {
    public static final String INT_TYPE = "INT_TYPE";

    public IntType() {
        super(INT_TYPE, "int");
    }
}

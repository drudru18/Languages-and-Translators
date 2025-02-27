package compiler.Lexer.Symbols.Keywords.Types;

import compiler.Lexer.Symbols.Keywords.TypeType;

public class BoolType extends TypeType {
    public static final String BOOL_TYPE = "BOOL_TYPE";

    public BoolType() {
        super(BOOL_TYPE, "bool");
    }
}
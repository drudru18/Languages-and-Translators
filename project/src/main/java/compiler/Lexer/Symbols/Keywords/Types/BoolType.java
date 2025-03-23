package compiler.Lexer.Symbols.Keywords.Types;

import compiler.Lexer.Symbols.Keywords.BasicType;

public class BoolType extends BasicType {
    public static final String BOOL_TYPE = "BOOL_TYPE";

    public BoolType() {
        super(BOOL_TYPE, "bool");
    }
}
package compiler.Lexer.Symbols.Keywords.Types;

import compiler.Lexer.Symbols.Keywords.BasicType;

public class FloatType extends BasicType {
    public static final String FLOAT_TYPE = "FLOAT_TYPE";

    public FloatType() {
        super(FLOAT_TYPE, "float");
    }
}
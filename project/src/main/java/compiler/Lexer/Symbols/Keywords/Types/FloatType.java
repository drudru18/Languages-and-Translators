package compiler.Lexer.Symbols.Keywords.Types;

import compiler.Lexer.Symbols.Keywords.TypeType;

public class FloatType extends TypeType {
    public static final String FLOAT_TYPE = "FLOAT_TYPE";

    public FloatType() {
        super(FLOAT_TYPE, "float");
    }
}
package compiler.Lexer.Symbols.Keywords.Types;

import compiler.Lexer.Symbols.Keywords.TypeType;

public class StringType extends TypeType {
    public static final String STRING_TYPE = "STRING_TYPE";

    public StringType() {
        super(STRING_TYPE, "string");
    }
}
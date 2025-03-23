package compiler.Lexer.Symbols.Keywords.Types;

import compiler.Lexer.Symbols.Keywords.BasicType;

public class StringType extends BasicType {
    public static final String STRING_TYPE = "STRING_TYPE";

    public StringType() {
        super(STRING_TYPE, "string");
    }
}
package compiler.Lexer;

public class Symbol {
    enum Type {
        KEYWORD, TYPE, IDENTIFIER, INTEGER, FLOAT, STRING, OPERATOR, DELIMITER, COMMENT, END
    }

    Type type;
    String value;

    Symbol(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "(" + type + ", " + value + ")";
    }
}

/**
 * here, the keywords are words like 'for', 'while', 'if', 'else'...
 *
 * the type is int, float, string, bool
 *
 * you can see that there are keywords for the type separately, they will be used for comparing later on
 * if the type matches the value
 *
 * the delimiters are the parenthesis, braces, brackets, or ;
 *
 * the END is the end of the string
 *
 * the identifier is the variable name or var word
 */
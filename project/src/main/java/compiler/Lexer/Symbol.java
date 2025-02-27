package compiler.Lexer;

public class Symbol {

    String type;
    String value;


    public Symbol(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return "(" + type + ", \"" + value + "\")";
    }
}
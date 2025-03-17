package compiler.Lexer;

public class Symbol {

    public String type;
    public String value;


    public Symbol(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return "(" + type + ", \"" + value + "\")";
    }
}
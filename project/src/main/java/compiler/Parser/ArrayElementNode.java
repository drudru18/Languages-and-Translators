package compiler.Parser;

public class ArrayElementNode extends ASTNode {
    String name;
    String array;

    ArrayElementNode(String name, String array) {
        this.name = name;
        this.array = array;
    }

    @Override
    public String toString() {
        return "";
    }
}

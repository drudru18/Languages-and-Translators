package compiler.Parser;

public class IdentifierNode extends ExpressionNode {
    public String name;

    public IdentifierNode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "";
    }
}

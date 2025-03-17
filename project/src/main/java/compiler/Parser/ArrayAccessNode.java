package compiler.Parser;

public class ArrayAccessNode extends ExpressionNode {
    public String array;
    public ExpressionNode index;

    public ArrayAccessNode(String array, ExpressionNode index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public String toString() {
        return "";
    }
}
package compiler.Parser;

public class BinaryOperationNode extends ExpressionNode {
    public ExpressionNode left;
    public String operator;
    public ExpressionNode right;

    public BinaryOperationNode(ExpressionNode left, String operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        return "";
    }
}

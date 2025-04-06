package compiler.Parser.CFG;

public class BinaryOperationNode extends ASTNode {
    private final String operator;
    private final ASTNode left;
    private final ASTNode right;

    public BinaryOperationNode(String operator, ASTNode left, ASTNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public ASTNode getLeft() {
        return left;
    }

    public ASTNode getRight() {
        return right;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("BinaryOp: ").append(operator).append("\n");
        printString.append(indentString).append("  Left\n");
        printString.append(left.toStringIndent(indent + 2));
        printString.append(indentString).append("  Right\n");
        printString.append(right.toStringIndent(indent + 2));
        return printString.toString();
    }
}

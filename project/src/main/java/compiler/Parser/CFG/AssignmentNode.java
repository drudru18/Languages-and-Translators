package compiler.Parser.CFG;

public class AssignmentNode extends ASTNode {
    private final ASTNode left;
    private final ASTNode right;

    public AssignmentNode(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
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
        printString.append(indentString).append("Assignment\n");
        printString.append(indentString).append("  Left\n");
        printString.append(left.toStringIndent(indent + 2));
        printString.append(indentString).append("  Right\n");
        printString.append(right.toStringIndent(indent + 2));
        return printString.toString();
    }
}

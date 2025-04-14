package compiler.Parser.CFG;

public class AssignmentNode extends ASTNode {
    public ASTNode left;
    public ASTNode right;

    public AssignmentNode(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
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

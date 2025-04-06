package compiler.Parser.CFG;

public class UnaryOperationNode extends ASTNode {
    private final ASTNode node;
    private final String operator;

    public UnaryOperationNode(String operator, ASTNode node) {
        this.node = node;
        this.operator = operator;
    }

    public ASTNode getNode() {
        return node;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("UnaryOp: ").append(operator).append("\n");
        printString.append(node.toStringIndent(indent + 1));
        return printString.toString();
    }
}

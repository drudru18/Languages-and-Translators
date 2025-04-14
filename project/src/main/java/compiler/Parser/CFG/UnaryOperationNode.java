package compiler.Parser.CFG;

public class UnaryOperationNode extends ASTNode {
    public ASTNode node;
    public String operator;

    public UnaryOperationNode(String operator, ASTNode node) {
        this.node = node;
        this.operator = operator;
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

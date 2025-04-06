package compiler.Parser.CFG;

public class ArrayAccessNode extends ASTNode {
    private final ASTNode arrayNode;
    private final ASTNode index;

    public ArrayAccessNode(ASTNode arrayNode, ASTNode index) {
        this.arrayNode = arrayNode;
        this.index = index;
    }

    public ASTNode getArrayNode() {
        return arrayNode;
    }

    public ASTNode getIndex() {
        return index;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("ArrayAccess\n");
        printString.append(indentString).append("  Array\n");
        printString.append(arrayNode.toStringIndent(indent + 2));
        printString.append(indentString).append("  Index\n");
        printString.append(index.toStringIndent(indent + 2));
        return printString.toString();
    }
}
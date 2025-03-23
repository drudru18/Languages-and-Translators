package compiler.Parser.CFG;

public class VariableDeclarationNode extends ASTNode {
    public boolean isFinal;
    public String varName;
    public TypeNode typeNode;
    public ASTNode valueOrIndexNode;

    public VariableDeclarationNode(boolean isFinal, String varName, TypeNode typeNode, ASTNode valueOrIndexNode) {
        this.isFinal = isFinal;
        this.varName = varName;
        this.typeNode = typeNode;
        this.valueOrIndexNode = valueOrIndexNode;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("VariableDeclaration\n");
        printString.append(indentString).append("  IsFinal: ").append(isFinal).append("\n");
        printString.append(indentString).append("  Name: ").append(varName).append("\n");
        printString.append(indentString).append("  Type\n");
        printString.append(typeNode.toStringIndent(indent + 2));
        if (valueOrIndexNode != null) {
            if (typeNode.isArray) {
                printString.append(indentString).append("  Capacity\n");
            }
            else {
                printString.append(indentString).append("  InitVal\n");
            }
            printString.append(valueOrIndexNode.toStringIndent(indent + 2));
        }
        return printString.toString();
    }
}


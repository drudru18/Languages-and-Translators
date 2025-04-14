package compiler.Parser.CFG;

public class VariableDeclarationNode extends ASTNode {
    public boolean isFinal;
    public String varName;
    public TypeNode typeNode;
    public ASTNode initializer;

    public VariableDeclarationNode(boolean isFinal, String varName, TypeNode typeNode, ASTNode initializer) {
        this.isFinal = isFinal;
        this.varName = varName;
        this.typeNode = typeNode;
        this.initializer = initializer;
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
        if (initializer != null) {
            printString.append(indentString).append("  InitVal\n");
            printString.append(initializer.toStringIndent(indent + 2));
        }
        return printString.toString();
    }
}


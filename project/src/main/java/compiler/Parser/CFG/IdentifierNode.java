package compiler.Parser.CFG;

public class IdentifierNode extends ASTNode {
    private final String name;

    public IdentifierNode(String name) {
        this.name = name;
    }

    public String getIdentifierName() {
        return name;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("Identifier: ").append(name).append("\n");
        return printString.toString();
    }
}

package compiler.Parser.CFG;

public class RecordAccessNode extends ASTNode {
    private final ASTNode recordNode;
    private final String name;

    public RecordAccessNode(ASTNode recordNode, String name) {
        this.recordNode = recordNode;
        this.name = name;
    }

    public ASTNode getRecordNode() {
        return recordNode;
    }

    public String getRecordName() {
        return name;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("RecordAccess\n");
        printString.append(indentString).append("  FieldName: ").append(name).append("\n");
        printString.append(indentString).append("  Variable\n");
        printString.append(recordNode.toStringIndent(indent + 2));
        return printString.toString();
    }
}
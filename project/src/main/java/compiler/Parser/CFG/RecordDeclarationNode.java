package compiler.Parser.CFG;

import java.util.ArrayList;

public class RecordDeclarationNode extends ASTNode {
    public String recordName;
    public ArrayList<VariableDeclarationNode> fields;

    public RecordDeclarationNode(String recordName, ArrayList<VariableDeclarationNode> fields) {
        this.recordName = recordName;
        this.fields = fields;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("RecordDeclaration\n");
        printString.append(indentString).append("  Name: ").append(recordName).append("\n");
        printString.append(indentString).append("  Fields\n");
        for (VariableDeclarationNode field : fields) {
            printString.append(field.toStringIndent(indent + 2));
        }
        return printString.toString();
    }
}

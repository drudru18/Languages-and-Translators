package compiler.Parser.CFG;

public class ReturnStatementNode extends ASTNode {
    public ASTNode value;

    public ReturnStatementNode(ASTNode value) {
        this.value = value;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("ReturnStatement\n");
        if (value != null) {
            printString.append(value.toStringIndent(indent + 1));
        }
        return printString.toString();
    }
}
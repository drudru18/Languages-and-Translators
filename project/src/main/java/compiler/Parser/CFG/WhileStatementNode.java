package compiler.Parser.CFG;

public class WhileStatementNode extends ASTNode {
    public ASTNode condition;
    public ASTNode body;

    public WhileStatementNode(ASTNode condition, ASTNode body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("WhileStatement\n");
        printString.append(indentString).append("  Condition\n");
        printString.append(condition.toStringIndent(indent + 2));
        printString.append(indentString).append("  Body\n");
        printString.append(body.toStringIndent(indent + 2));
        return printString.toString();
    }
}
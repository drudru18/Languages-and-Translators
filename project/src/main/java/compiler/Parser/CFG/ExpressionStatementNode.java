package compiler.Parser.CFG;

public class ExpressionStatementNode extends ASTNode {
    ASTNode expression;

    public ExpressionStatementNode(ASTNode expression) {
        this.expression = expression;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("Expression\n");
        printString.append(expression.toStringIndent(indent + 1));
        return printString.toString();
    }
}

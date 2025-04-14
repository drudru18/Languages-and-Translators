package compiler.Parser.CFG;

public class ForStatementNode extends ASTNode {
    public ASTNode variable;
    public ASTNode initialValue;
    public ASTNode maxValue;
    public ASTNode incrementValue;
    public ASTNode body;


    public ForStatementNode(ASTNode variable, ASTNode initialValue, ASTNode maxValue, ASTNode incrementValue, ASTNode body) {
        this.variable = variable;
        this.initialValue = initialValue;
        this.maxValue = maxValue;
        this.incrementValue = incrementValue;
        this.body = body;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("ForStatement\n");
        printString.append(indentString).append("  ForVariable\n");
        printString.append(variable.toStringIndent(indent + 2));
        printString.append(indentString).append("  InitialValue\n");
        printString.append(initialValue.toStringIndent(indent + 2));
        printString.append(indentString).append("  MaxValue\n");
        printString.append(maxValue.toStringIndent(indent + 2));
        printString.append(indentString).append("  Increment\n");
        printString.append(incrementValue.toStringIndent(indent + 2));
        printString.append(indentString).append("  Body\n");
        printString.append(body.toStringIndent(indent + 2));
        return printString.toString();
    }
}
package compiler.Parser.CFG;

public class VariableNode extends ASTNode {
    public ASTNode variable;

    public VariableNode(ASTNode variable) {
        this.variable = variable;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("Variable\n");
        printString.append(variable.toStringIndent(indent + 2));
        return printString.toString();
    }
}


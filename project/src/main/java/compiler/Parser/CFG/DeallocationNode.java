package compiler.Parser.CFG;

public class DeallocationNode extends ASTNode {
    public ASTNode variable;

    public DeallocationNode(ASTNode variable) {
        this.variable = variable;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("Deallocation\n");
        printString.append(variable.toStringIndent(indent + 1));
        return printString.toString();
    }
}
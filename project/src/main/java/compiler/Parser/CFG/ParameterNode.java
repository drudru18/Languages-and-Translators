package compiler.Parser.CFG;

public class ParameterNode extends ASTNode {
    public String paramName;
    public ASTNode type;

    public ParameterNode(String paramName, ASTNode type) {
        this.paramName = paramName;
        this.type = type;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("Parameter\n");
        printString.append(indentString).append("  Name: ").append(paramName).append("\n");
        printString.append(indentString).append("  Type\n");
        printString.append(type.toStringIndent(indent + 2));
        return printString.toString();
    }
}

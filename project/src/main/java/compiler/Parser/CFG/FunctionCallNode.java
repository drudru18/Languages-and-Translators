package compiler.Parser.CFG;

import java.util.List;

public class FunctionCallNode extends ASTNode {
    private final ASTNode functionNode; // name of the called function
    private final List<ASTNode> arguments; // the arguments inside it

    public FunctionCallNode(ASTNode functionNode, List<ASTNode> arguments) {
        this.functionNode = functionNode;
        this.arguments = arguments;
    }

    public ASTNode getFunctionNode() {
        return functionNode;
    }

    public List<ASTNode> getArguments() {
        return arguments;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("FunctionCall\n");
        printString.append(indentString).append("  Function\n");
        printString.append(functionNode.toStringIndent(indent + 2));
        printString.append(indentString).append("  Arguments\n");
        for (ASTNode arg : arguments) {
            printString.append(arg.toStringIndent(indent + 2));
        }
        return printString.toString();
    }
}

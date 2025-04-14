package compiler.Parser.CFG;

import java.util.ArrayList;
import java.util.List;

public class FunctionCallNode extends ASTNode {
    public ASTNode functionNode;
    public ArrayList<ASTNode> arguments;

    public FunctionCallNode(ASTNode functionNode, ArrayList<ASTNode> arguments) {
        this.functionNode = functionNode;
        this.arguments = arguments;
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

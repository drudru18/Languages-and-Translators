package compiler.Parser.CFG;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends ASTNode {
    public ArrayList<ASTNode> nodes;

    public ProgramNode(ArrayList<ASTNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("Program\n");
        for (ASTNode node : nodes) {
            printString.append(node.toStringIndent(indent + 1));
        }
        return printString.toString();
    }
}

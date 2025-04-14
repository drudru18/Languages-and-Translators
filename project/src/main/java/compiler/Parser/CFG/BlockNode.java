package compiler.Parser.CFG;

import java.util.ArrayList;
import java.util.List;

public class BlockNode extends ASTNode {
    public ArrayList<ASTNode> statements;

    public BlockNode(ArrayList<ASTNode> statements) {
        this.statements = statements;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("Block\n");
        for (ASTNode statement : statements) {
            printString.append(statement.toStringIndent(indent + 1));
        }
        return printString.toString();
    }
}
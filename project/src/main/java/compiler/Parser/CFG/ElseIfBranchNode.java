package compiler.Parser.CFG;

import java.util.ArrayList;

public class ElseIfBranchNode extends ASTNode {
    ASTNode condition;
    ASTNode block;

    public ElseIfBranchNode(ASTNode condition, ASTNode block) {
        this.condition = condition;
        this.block = block;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("ElseIfBranch\n");
        printString.append(indentString).append("  Condition\n");
        printString.append(condition.toStringIndent(indent + 2));
        printString.append(indentString).append("  ElseIfBlock\n");
        printString.append(block.toStringIndent(indent + 2));
        return printString.toString();
    }
}
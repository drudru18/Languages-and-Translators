package compiler.Parser.CFG;

import java.util.ArrayList;

public class IfStatementNode extends ASTNode {
    ASTNode condition;
    ASTNode ifBlock;
    ArrayList<ElseIfBranchNode> elseIfBranches;
    ASTNode elseBlock;

    public IfStatementNode(ASTNode condition, ASTNode ifBlock, ArrayList<ElseIfBranchNode> elseIfBranches, ASTNode elseBlock) {
        this.condition = condition;
        this.ifBlock = ifBlock;
        this.elseIfBranches = elseIfBranches;
        this.elseBlock = elseBlock;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("IfStatement\n");
        printString.append(indentString).append("  Condition\n");
        printString.append(condition.toStringIndent(indent + 2));
        printString.append(indentString).append("  IfBlock\n");
        printString.append(ifBlock.toStringIndent(indent + 2));
        if (!elseIfBranches.isEmpty()) {
            printString.append(indentString).append("  ElseIfBranches\n");
            for (ElseIfBranchNode elseIfBranch : elseIfBranches) {
                printString.append(elseIfBranch.toStringIndent(indent + 2));
            }
        }
        if (elseBlock != null) {
            printString.append(indentString).append("  ElseBlock\n");
            printString.append(elseBlock.toStringIndent(indent + 2));
        }
        return printString.toString();
    }
}
package compiler.Parser;

import java.util.List;

public class BlockNode extends StatementNode {
    public List<StatementNode> statements;

    public BlockNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "";
    }
}
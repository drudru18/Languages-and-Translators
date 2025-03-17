package compiler.Parser;

import java.util.List;

public class ProgramNode extends ASTNode {
    public List<StatementNode> statements;

    public ProgramNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProgramNode:\n");
        for (StatementNode stmt : statements) {
            sb.append(stmt.toString()).append("\n");
        }
        return sb.toString();
    }
}

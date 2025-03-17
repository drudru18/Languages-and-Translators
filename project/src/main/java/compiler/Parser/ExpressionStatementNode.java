package compiler.Parser;

public class ExpressionStatementNode extends StatementNode {
    ExpressionNode expression;

    public ExpressionStatementNode(ExpressionNode expression) {
        this.expression = expression;
    }
    @Override
    public String toString() {
        return "";
    }
}

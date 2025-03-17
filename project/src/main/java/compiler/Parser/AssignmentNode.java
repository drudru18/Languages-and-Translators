package compiler.Parser;

public class AssignmentNode extends StatementNode {
    public String name;
    public ExpressionNode expression;

    public AssignmentNode(String name, ExpressionNode expression) {
        this.name = name;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "";
    }
}

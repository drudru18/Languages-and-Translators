package compiler.Parser;

public class VariableDeclarationNode extends StatementNode {
    public String type;
    public String name;
    public ExpressionNode expression;

    public VariableDeclarationNode(String type, String name, ExpressionNode expression) {
        this.type = type;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "Identifier " + name + " of type " + type + " of expression " + expression;
    }
}


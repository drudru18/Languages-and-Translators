package compiler.Parser.CFG;

public class VariableNode extends ASTNode {
    public ASTNode variable;

    public VariableNode(ASTNode variable) {
        this.variable = variable;
    }

    @Override
    public String toStringIndent(int indent) {
        return variable.toStringIndent(indent);
    }
}


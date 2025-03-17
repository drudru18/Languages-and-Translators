package compiler.Parser;

public class ReturnNode extends StatementNode {
    private ExpressionNode returnValue;

    public ReturnNode(ExpressionNode returnValue) {
        this.returnValue = returnValue;
    }

    public ExpressionNode getReturnValue() {
        return returnValue;
    }

    @Override
    public String toString() {
        return "";
    }
}
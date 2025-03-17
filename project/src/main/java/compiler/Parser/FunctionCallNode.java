package compiler.Parser;

import java.util.List;

public class FunctionCallNode extends ExpressionNode {
    public String functionName;
    public List<ExpressionNode> arguments;

    public FunctionCallNode(String functionName, List<ExpressionNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "";
    }
}

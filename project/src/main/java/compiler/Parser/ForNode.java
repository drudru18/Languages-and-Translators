package compiler.Parser;

import java.util.List;

public class ForNode extends StatementNode {
    private String loopVariable;
    private ExpressionNode initialValue;
    private ExpressionNode maxValue;
    private ExpressionNode increment;
    private List<StatementNode> body;

    public ForNode(String loopVariable, ExpressionNode initialValue, ExpressionNode maxValue, ExpressionNode increment, List<StatementNode> body) {
        this.loopVariable = loopVariable;
        this.initialValue = initialValue;
        this.maxValue = maxValue;
        this.increment = increment;
        this.body = body;
    }

    public String getLoopVariable() {
        return loopVariable;
    }

    public ExpressionNode getInitialValue() {
        return initialValue;
    }

    public ExpressionNode getMaxValue() {
        return maxValue;
    }

    public ExpressionNode getIncrement() {
        return increment;
    }

    public List<StatementNode> getBody() {
        return body;
    }


    @Override
    public String toString() {
        return "";
    }
}
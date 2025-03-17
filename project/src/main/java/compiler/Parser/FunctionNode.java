package compiler.Parser;

import java.util.List;

public class FunctionNode extends StatementNode {
    public String name;
    public String returnType;
    public List<ParameterNode> parameters;
    public List<StatementNode> body;

    public FunctionNode(String name, List<ParameterNode> parameters, String returnType, List<StatementNode> body) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.body = body;
    }

    @Override
    public String toString() {
        return "";
    }
}
package compiler.Parser.CFG;

import java.util.ArrayList;

public class FunctionDeclarationNode extends ASTNode {
    private final String functionName;
    private final ArrayList<ParameterNode> parameters;
    private final ASTNode returnType;
    private final ASTNode functionBody;

    public FunctionDeclarationNode(String functionName, ArrayList<ParameterNode> parameters, ASTNode returnType, ASTNode functionBody) {
        this.functionName = functionName;
        this.parameters = parameters;
        this.returnType = returnType;
        this.functionBody = functionBody;
    }

    public String getFunctionName() {
        return functionName;
    }

    public ArrayList<ParameterNode> getParameters() {
        return parameters;
    }

    public ASTNode getReturnType() {
        return returnType;
    }

    public ASTNode getFunctionBody() {
        return functionBody;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("FunctionDeclaration\n");
        printString.append(indentString).append("  Name: ").append(functionName).append("\n");
        printString.append(indentString).append("  Parameters\n");
        for (ParameterNode parameter : parameters) {
            printString.append(parameter.toStringIndent(indent + 2));
        }
        if (returnType != null) {
            printString.append(indentString).append("  ReturnType\n");
            printString.append(returnType.toStringIndent(indent + 2));
        }
        else {
            printString.append(indentString).append("  ReturnType: void\n");
        }
        printString.append(indentString).append("  Body\n");
        printString.append(functionBody.toStringIndent(indent + 2));
        return printString.toString();
    }
}


package compiler.Parser;

public class ASTPrinter {
    public static void print(ASTNode node) {
        print(node, 0);
    }

    private static void print(ASTNode node, int indent) {
        if (node == null) return;
        printIndent(indent);

        if (node instanceof ProgramNode) {
            System.out.println("Program:");
            for (StatementNode stmt : ((ProgramNode) node).statements) {
                print(stmt, indent + 1);
            }
        } else if (node instanceof FunctionNode) {
            FunctionNode fn = (FunctionNode) node;
            System.out.println("Function: " + fn.name + " returns " + fn.returnType);
            printIndent(indent + 1);
            System.out.println("Parameters:");
            for (ParameterNode param : fn.parameters) {
                printIndent(indent + 2);
                System.out.println(param.type + " " + param.name + (param.isArray ? "[]" : ""));
            }
            printIndent(indent + 1);
            System.out.println("Body:");
            for (StatementNode stmt : fn.body) {
                print(stmt, indent + 2);
            }
        } else if (node instanceof VariableDeclarationNode) {
            VariableDeclarationNode varDecl = (VariableDeclarationNode) node;
            System.out.println("Variable Declaration: " + varDecl.type + " " + varDecl.name);
            print(varDecl.expression, indent + 1);
        } else if (node instanceof ReturnNode) {
            System.out.println("Return:");
            print(((ReturnNode) node).getReturnValue(), indent + 1);
        } else if (node instanceof AssignmentNode) {
            AssignmentNode assign = (AssignmentNode) node;
            System.out.println("Assignment: " + assign.name);
            print(assign.expression, indent + 1);
        } else if (node instanceof ExpressionStatementNode) {
            System.out.println("Expression Statement:");
            print(((ExpressionStatementNode) node).expression, indent + 1);
        } else if (node instanceof BinaryOperationNode) {
            BinaryOperationNode binOp = (BinaryOperationNode) node;
            System.out.println("Binary Operation: " + binOp.operator);
            print(binOp.left, indent + 1);
            print(binOp.right, indent + 1);
        } else if (node instanceof FunctionCallNode) {
            FunctionCallNode funcCall = (FunctionCallNode) node;
            String functionName = funcCall.functionName;
            if (functionName.charAt(0) >= 97 && functionName.charAt(0) <= 122) {
                System.out.println("Function Call: " + functionName);
                printIndent(indent + 1);
                System.out.println("Arguments:");
                for (ExpressionNode arg : funcCall.arguments) {
                    print(arg, indent + 2);
                }
            } else if (functionName.charAt(0) >= 65 && functionName.charAt(0) <= 90) {
                System.out.println("Record: " + functionName);
                printIndent(indent + 1);
                System.out.println("Elements:");
                for (ExpressionNode arg : funcCall.arguments) {
                    print(arg, indent + 2);
                }
            }
        } else if (node instanceof ArrayAccessNode) {
            ArrayAccessNode arrayAccess = (ArrayAccessNode) node;
            System.out.println("Array Access: " + arrayAccess.array);
            print(arrayAccess.index, indent + 1);
        } else if (node instanceof ArrayElementNode) {
            ArrayElementNode element = (ArrayElementNode) node;
            System.out.println("Array Element Access: " + element.name + " of " + element.name);
        } else if (node instanceof IdentifierNode) {
            System.out.println("Identifier: " + ((IdentifierNode) node).name);
        } else if (node instanceof NumberNode) {
            NumberNode number = (NumberNode) node;
            System.out.println("Number: " + number.value + " (" + number.type + ")");
        } else {
            System.out.println("Unknown Node Type: " + node.getClass().getSimpleName());
        }
    }

    private static void printIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
    }
}

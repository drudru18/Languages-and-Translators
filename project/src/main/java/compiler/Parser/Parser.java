package compiler.Parser;
import compiler.Lexer.*;
import compiler.Lexer.Symbol;

import java.util.*;

public class Parser {
    private final Lexer lexer;
    private Symbol currentSymbol;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentSymbol = lexer.getNextSymbol();
    }

    private final HashSet<String> operators = new HashSet<>(Arrays.asList(
            "PLUS_OPERATOR", "MINUS_OPERATOR", "MULTIPLY_OPERATOR",
            "DIVIDE_OPERATOR", "MODULO_OPERATOR", "EQUALS_OPERATOR",
            "AND_OPERATOR", "OR_OPERATOR", "DIFFERENT_OPERATOR",
            "GREATER_OR_EQUALS_OPERATOR", "LOWER_OR_EQUALS_OPERATOR",
            "STRICTLY_GREATER_THAN_OPERATOR", "STRICTLY_LOWER_THAN_OPERATOR"
    ));

    private void advance() {
        currentSymbol = lexer.getNextSymbol();
    }

    private void expect(String expectedType) {
        if (!currentSymbol.type.equals(expectedType)) {
            throw new RuntimeException("Syntax Error: Expected " + expectedType + " but got " + currentSymbol.type);
        }
        advance();
    }

    public ASTNode getAST() {
        return parseProgram();
    }

    private ProgramNode parseProgram() {
        List<StatementNode> statements = new ArrayList<>();

        while (!currentSymbol.type.equals("END_OF_INPUT")) {
            statements.add(parseStatement());
        }
        return new ProgramNode(statements);
    }

    private StatementNode parseStatement() {
        if (currentSymbol.type.equals("FUN_KEYWORD")) {
            return parseFunctionDefinition();
        } else if (currentSymbol.type.equals("RETURN_KEYWORD")) {
            return parseReturnStatement();
        } else if (currentSymbol.value.equals("var")) {
            return parseVariableDeclaration();
        } else {
            return parseExpressionStatement();
        }
    }

    private FunctionNode parseFunctionDefinition() {
        expect("FUN_KEYWORD");
        String functionName = currentSymbol.value;
        expect("IDENTIFIER");
        expect("LEFT_PARENTHESIS");

        List<ParameterNode> parameters = new ArrayList<>();
        while (!currentSymbol.type.equals("RIGHT_PARENTHESIS")) {
            String paramType = currentSymbol.value;
            char determineType = currentSymbol.value.charAt(0);

            if (determineType >= 97 && determineType <= 122) {
                if (currentSymbol.value.equals("int")) expect("INT_TYPE");
                else if (currentSymbol.type.equals("float")) expect("FLOAT_TYPE");
                else if (currentSymbol.type.equals("bool")) expect("BOOL_TYPE");
                else if (currentSymbol.type.equals("string")) expect("STRING_TYPE");
                else throw new RuntimeException("The type " + currentSymbol.value + " is not supported");
            }
            else if (determineType >= 65 && determineType <= 90) expect("RECORD_IDENTIFIER");

            boolean isArray = false;
            if (currentSymbol.type.equals("LEFT_BRACKET")) {
                expect("LEFT_BRACKET");
                expect("RIGHT_BRACKET");
                isArray = true;
            }
            String paramName = currentSymbol.value;
            expect("IDENTIFIER");
            parameters.add(new ParameterNode(paramType, paramName, isArray));
            if (currentSymbol.type.equals("COMMA")) {
                advance();
            }
        }
        expect("RIGHT_PARENTHESIS");

        String returnType = currentSymbol.value;
        char determineType = currentSymbol.value.charAt(0);
        if (determineType >= 97 && determineType <= 122) {
            if (currentSymbol.value.equals("int")) expect("INT_TYPE");
            else if (currentSymbol.type.equals("float")) expect("FLOAT_TYPE");
            else if (currentSymbol.type.equals("bool")) expect("BOOL_TYPE");
            else if (currentSymbol.type.equals("string")) expect("STRING_TYPE");
            else throw new RuntimeException("The type " + currentSymbol.value + " is not supported");
        }
        else if (determineType >= 65 && determineType <= 90) expect("RECORD_IDENTIFIER");

        boolean isReturnTypeArray = false;
        if (currentSymbol.type.equals("LEFT_BRACKET")) {
            expect("LEFT_BRACKET");
            expect("RIGHT_BRACKET");
            isReturnTypeArray = true;
        }

        expect("LEFT_BRACE");

        List<StatementNode> body = new ArrayList<>();
        while (!currentSymbol.type.equals("RIGHT_BRACE")) {
            body.add(parseStatement());
        }
        expect("RIGHT_BRACE");

        return new FunctionNode(functionName, parameters, returnType, body, isReturnTypeArray);
    }

    private ReturnNode parseReturnStatement() {
        expect("RETURN_KEYWORD");
        ExpressionNode returnValue = parseExpression();
        expect("SEMICOLON");
        return new ReturnNode(returnValue);
    }

    private VariableDeclarationNode parseVariableDeclaration() {
        expect("IDENTIFIER");
        String varName = currentSymbol.value;
        expect("IDENTIFIER");
        String varType = currentSymbol.value;
        switch (currentSymbol.type) {
            case "FLOAT_TYPE":
                expect("FLOAT_TYPE");
                break;
            case "INT_TYPE":
                expect("INT_TYPE");
                break;
            case "STRING_TYPE":
                expect("STRING_TYPE");
                break;
            case "BOOL_TYPE":
                expect("BOOL_TYPE");
                break;
        }
        expect("ASSIGNMENT");
        ExpressionNode initializer = parseExpression();
        expect("SEMICOLON");
        return new VariableDeclarationNode(varType, varName, initializer);
    }

    private StatementNode parseExpressionStatement() {
        ExpressionNode expr = parseExpression();

        // Check if it's an assignment
        if (expr instanceof IdentifierNode && currentSymbol.type.equals("ASSIGNMENT")) {
            advance(); // Consume '='
            ExpressionNode value = parseExpression();
            expect("SEMICOLON");
            return new AssignmentNode(((IdentifierNode) expr).name, value);
        }

        // Otherwise, treat it as a standalone expression statement
        expect("SEMICOLON");
        return new ExpressionStatementNode(expr);
    }

    private ExpressionNode parseExpression() {
        ExpressionNode left = parsePrimaryExpression();
        while (operators.contains(currentSymbol.type)) {
            String operator = currentSymbol.value;
            advance();
            ExpressionNode right = parsePrimaryExpression();
            left = new BinaryOperationNode(left, operator, right);
        }
        return left;
    }

    private ExpressionNode parsePrimaryExpression() {
        if (currentSymbol.type.equals("IDENTIFIER") || currentSymbol.type.equals("RECORD_IDENTIFIER")) {
            String identifier = currentSymbol.value;
            advance();

            if (currentSymbol.type.equals("LEFT_PARENTHESIS")) {
                return parseFunctionOrConstructorCall(identifier);
            } else if (currentSymbol.type.equals("LEFT_BRACKET")) {
                return parseArrayAccess(identifier);
            }
            return new IdentifierNode(identifier);
        } else if (currentSymbol.type.equals("INTEGER_NUMBER")) {
            ExpressionNode node = new NumberNode(currentSymbol.value, currentSymbol.type);
            advance();
            return node;
        } else if (currentSymbol.type.equals("FLOAT_NUMBER")) {
            ExpressionNode node = new NumberNode(currentSymbol.value, currentSymbol.type);
            advance();
            return node;
        } else if (currentSymbol.type.equals("LEFT_PARENTHESIS")) {
            advance();
            ExpressionNode expr = parseExpression();
            expect("RIGHT_PARENTHESIS");
            return expr;
        } else if (currentSymbol.type.equals("LEFT_BRACKET")) {
            advance();
            ExpressionNode expr = parseExpression();
            expect("RIGHT_BRACKET");
            return expr;
        }
        throw new RuntimeException("Syntax Error: Unexpected token " + currentSymbol);
    }

    private ExpressionNode parseFunctionOrConstructorCall(String functionName) {
        expect("LEFT_PARENTHESIS");  // Expect '(' after function name
        List<ExpressionNode> arguments = new ArrayList<>();

        // Parse function arguments (comma-separated)
        if (!currentSymbol.type.equals("RIGHT_PARENTHESIS")) {
            do {
                arguments.add(parseExpression());
                if (currentSymbol.type.equals("COMMA")) {
                    advance();
                } else {
                    break;
                }
            } while (true);
        }

        expect("RIGHT_PARENTHESIS");  // Expect ')'

        return new FunctionCallNode(functionName, arguments);
    }

    private ExpressionNode parseArrayAccess(String arrayName) {
        expect("LEFT_BRACKET"); // Expect '[' after array name
        List<ExpressionNode> arguments = new ArrayList<>();

        if (!currentSymbol.type.equals("RIGHT_BRACKET")) {
            do {
                arguments.add(parseExpression());
                if (currentSymbol.type.equals("COMMA")) {
                    throw new RuntimeException("Your list access has more than one argument");
                } else {
                    break;
                }
            } while (true);
        }
        expect("RIGHT_BRACKET");  // Expect ']'

        if (currentSymbol.type.equals("DOT")) {
            parseArrayElementAccess(arrayName);
        } else if (currentSymbol.type.equals("LEFT_BRACKET")) { // we check for matrix
            parseArrayAccess(arrayName);
        }

        return new ArrayAccessNode(arrayName, arguments.get(0));
    }

    private void parseArrayElementAccess(String arrayName) {
        expect("DOT");
        String currentElement = currentSymbol.value;
        expect("IDENTIFIER");
        new ArrayElementNode(currentElement, arrayName);
    }
}

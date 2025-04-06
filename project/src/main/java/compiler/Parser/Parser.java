package compiler.Parser;

import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Lexer.Symbols.*;
import compiler.Lexer.Symbols.Delimiters.*;
import compiler.Lexer.Symbols.Keywords.*;
import compiler.Lexer.Symbols.Keywords.Types.BoolType;
import compiler.Lexer.Symbols.Keywords.Types.FloatType;
import compiler.Lexer.Symbols.Keywords.Types.IntType;
import compiler.Lexer.Symbols.Keywords.Types.StringType;
import compiler.Lexer.Symbols.Numbers.FloatNumber;
import compiler.Lexer.Symbols.Numbers.IntegerNumber;
import compiler.Lexer.Symbols.Operators.*;
import compiler.Parser.CFG.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    public final Lexer lexer;
    public Symbol currentSymbol;
    ArrayList<Symbol> symbols;
    public int currentSymbolIndex;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        currentSymbol = lexer.getNextSymbol();
        // Put all the symbols in an array for easier backtracking
        symbols = new ArrayList<>();
        symbols.add(currentSymbol);
        while (!typeEquals(EndOfInputType.class)){
            currentSymbol = lexer.getNextSymbol();
            symbols.add(currentSymbol);
        }
        currentSymbolIndex = 0;
        currentSymbol = symbols.get(currentSymbolIndex);
    }

    // Go to the next symbol
    public void advance() {
        currentSymbolIndex++;
        currentSymbol = symbols.get(currentSymbolIndex);
    }

    // Force match and advance
    private void expect(Class<?> expectedType) {
        if (!typeEquals(expectedType)) {
            throw new RuntimeException("Syntax Error: Expected " + expectedType.getSimpleName() + " but got " + currentSymbol.type);
        }
        advance();
    }

    // Go backwards with one symbol
    public void backtrack() {
        currentSymbolIndex--;
        currentSymbol = symbols.get(currentSymbolIndex);
    }

    // Test is the current symbol is the same as the expected one
    public boolean typeEquals(Class<?> expectedType) {
        return expectedType.isInstance(currentSymbol);
    }

    // Returns the root of the AST
    public ASTNode getAST() {
        return parseProgram();
    }

    // Parses the entire program from the beginning of the file (or the string)
    public ProgramNode parseProgram() {
        // Store the children of the root in nodes...
        ArrayList<ASTNode> nodes = new ArrayList<>();
        // ...until the end of file symbol is found
        while (!typeEquals(EndOfInputType.class)) {
            nodes.add(parseNodes());
        }
        return new ProgramNode(nodes);
    }

    // Parses the CFG's that can be found outside the
    // functions and records
    public ASTNode parseNodes() {
        if (typeEquals(FinalKeyword.class)) {
            return parseVariableDeclaration();
        }
        else if (typeEquals(FunKeyword.class)) {
            return parseFunctionDeclaration();
        }
        else if (typeEquals(IdentifierType.class)) {
            return parseVariableDeclaration();
        }
        else if (typeEquals(RecordIdentifierType.class)) {
                return parseRecordDeclaration();
        }
        throw new RuntimeException("Syntax Error: First symbol not allowed outside functions/records");
    }

    // Entry point for parsing expressions
    public ASTNode parseExpression() {
        return new ExpressionStatementNode(parseLogicalOr());
    }

    // LogicalOr -> LogicalAnd ("||" LogicalAnd)*
    private ASTNode parseLogicalOr() {
        ASTNode node = parseLogicalAnd();

        while (typeEquals(OrOperator.class)) {
            String operator = currentSymbol.value;
            expect(OrOperator.class);
            ASTNode right = parseLogicalAnd();
            node = new BinaryOperationNode(operator, node, right);
        }

        return node;
    }

    // LogicalAnd -> Equality ("&&" Equality)*
    private ASTNode parseLogicalAnd() {
        ASTNode node = parseEquality();

        while (typeEquals(AndOperator.class)) {
            String operator = currentSymbol.value;
            expect(AndOperator.class);
            ASTNode right = parseEquality();
            node = new BinaryOperationNode(operator, node, right);
        }

        return node;
    }

    // Equality -> Comparison (("==" | "!=") Comparison)*
    private ASTNode parseEquality() {
        ASTNode node = parseComparison();

        while (typeEquals(EqualsOperator.class) || typeEquals(DifferentOperator.class)) {
            String operator = currentSymbol.value;
            expect(currentSymbol.getClass());
            ASTNode right = parseComparison();
            node = new BinaryOperationNode(operator, node, right);
        }

        return node;
    }

    // Comparison -> Term (("<" | ">" | "<=" | ">=") Term)*
    private ASTNode parseComparison() {
        ASTNode node = parseTerm();

        while (typeEquals(StrictlyLowerThanOperator.class) || typeEquals(StrictlyGreaterThanOperator.class) ||
                typeEquals(LowerOrEqualsOperator.class) || typeEquals(GreaterOrEqualsOperator.class)) {
            String operator = currentSymbol.value;
            expect(currentSymbol.getClass());
            ASTNode right = parseTerm();
            node = new BinaryOperationNode(operator, node, right);
        }

        return node;
    }

    // Term -> Factor (("+" | "-") Factor)*
    private ASTNode parseTerm() {
        ASTNode node = parseFactor();

        while (typeEquals(PlusOperator.class) || typeEquals(MinusOperator.class)) {
            String operator = currentSymbol.value;
            expect(currentSymbol.getClass());
            ASTNode right = parseFactor();
            node = new BinaryOperationNode(operator, node, right);
        }

        return node;
    }

    // Factor -> Unary (("*" | "/" | "%") Unary)*
    private ASTNode parseFactor() {
        ASTNode node = parseUnary();

        while (typeEquals(MultiplyOperator.class) || typeEquals(DivideOperator.class) ||
                typeEquals(ModuloOperator.class)) {
            String operator = currentSymbol.value;
            expect(currentSymbol.getClass());
            ASTNode right = parseUnary();
            node = new BinaryOperationNode(operator, node, right);
        }

        return node;
    }

    // Unary -> ("-" | "!")? Primary
    private ASTNode parseUnary() {
        if (typeEquals(MinusOperator.class) || typeEquals(Not.class)) {
            String operator = currentSymbol.value;
            expect(currentSymbol.getClass());
            ASTNode node = parsePrimary();
            return new UnaryOperationNode(operator, node);
        }

        return parsePrimary();
    }

    // Primary -> IntegerLiteral | FloatLiteral | BooleanLiteral | StringLiteral | Identifier | FunctionCall | "(" Expression ")" | ArrayAccess | RecordAccess
    private ASTNode parsePrimary() {
        if (typeEquals(IntegerNumber.class) || typeEquals(FloatNumber.class) ||
                typeEquals(TrueKeyword.class) || typeEquals(FalseKeyword.class) ||
                typeEquals(StringValue.class)) {
            ASTNode node = new LiteralNode(currentSymbol);
            expect(currentSymbol.getClass());
            return node;
        }

        ASTNode node = null;

        // Identifier: Can be a variable, function call, record field, or array access
        if (typeEquals(IdentifierType.class) || typeEquals(RecordIdentifierType.class)) {
            String name = currentSymbol.value;
            expect(currentSymbol.getClass());
            node = new IdentifierNode(name);
        }
        // Parenthesized Expression: (expression)
        else if (typeEquals(LeftParenthesis.class)) {
            expect(LeftParenthesis.class);
            node = parseExpression();
            expect(RightParenthesis.class);
        }
        else {
            throw new RuntimeException("Unexpected token: " + currentSymbol.type);
        }

        // Chained accesses: Array accesses, field accesses
        while (typeEquals(LeftParenthesis.class) ||
                typeEquals(LeftBracket.class) ||
                typeEquals(Dot.class)) {

            // Function call: `Identifier(...)`
            if (typeEquals(LeftParenthesis.class)) {
                node = parseFunctionCall(node);
            }
            // Array access: `Identifier[...]`
            else if (typeEquals(LeftBracket.class)) {
                node = parseArrayAccess(node);
            }
            // Field access: `Identifier.Identifier`
            else if (typeEquals(Dot.class)) {
                node = parseRecordAccess(node);
            }
        }

        return node;
    }

    // FunctionCall -> "(" ArgumentList? ")"
    private ASTNode parseFunctionCall(ASTNode functionNode) {
        expect(LeftParenthesis.class);
        List<ASTNode> arguments = new ArrayList<>();

        if (!typeEquals(RightParenthesis.class)) {
            arguments.add(parseExpression());
            while (typeEquals(Comma.class)) {
                expect(Comma.class);
                arguments.add(parseExpression());
            }
        }

        expect(RightParenthesis.class);
        return new FunctionCallNode(functionNode, arguments);
    }

    // ArrayAccess -> Expression "[" Expression "]"
    private ASTNode parseArrayAccess(ASTNode arrayNode) {
        expect(LeftBracket.class);
        ASTNode index = parseExpression();
        expect(RightBracket.class);
        return new ArrayAccessNode(arrayNode, index);
    }

    // RecordAccess -> Expression "." Identifier
    private ASTNode parseRecordAccess(ASTNode recordNode) {
        expect(Dot.class);
        if (!typeEquals(IdentifierType.class)) {
            throw new RuntimeException("Expected field name after '.'");
        }
        String field = currentSymbol.value;
        expect(IdentifierType.class);
        return new RecordAccessNode(recordNode, field);
    }

    // Parses a type that also contains information about
    // whether the variable is an array or not
    private TypeNode parseType() {
        if (typeEquals(IntType.class) ||
                typeEquals(FloatType.class) ||
                typeEquals(BoolType.class) ||
                typeEquals(StringType.class) ||
                typeEquals(RecordIdentifierType.class)) {

            Symbol varType = currentSymbol;
            expect(currentSymbol.getClass());

            if (typeEquals(LeftBracket.class)) {
                expect(LeftBracket.class);
                expect(RightBracket.class);
                return new TypeNode(varType, true);
            }

            return new TypeNode(varType, false);
        }

        throw new RuntimeException("Expected a type, but found: " + currentSymbol.type);
    }

    // Parses a variable declaration that contains info about
    // whether the var is final, the type, the name and the initial
    // value or array capacity
    private ASTNode parseVariableDeclaration() {
        boolean isFinal = false;

        // Check for 'final' keyword
        if (typeEquals(FinalKeyword.class)) {
            isFinal = true;
            expect(FinalKeyword.class);
        }

        // Expect identifier (variable name)
        if (!typeEquals(IdentifierType.class)) {
            throw new RuntimeException("Expected variable name, but found: " + currentSymbol.type);
        }
        String varName = currentSymbol.value;
        expect(IdentifierType.class);

        // Expect type
        TypeNode typeNode = parseType();
        if (isFinal && typeNode.getTypeNode() instanceof IdentifierType) {
            throw new RuntimeException("Final variable cannot have record type");
        }

        // Optional initialization that is value or index of array
        ASTNode initializer = null;
        if (typeEquals(Assignment.class)) {
            expect(Assignment.class);
            if (typeNode.isArray()) {
                expect(ArrayKeyword.class);
                expect(OfKeyword.class);
                expect(LeftBracket.class);
            }
            initializer = parseExpression();
            if (typeNode.isArray()) {
                expect(RightBracket.class);
            }
        }

        // Expect semicolon
        expect(Semicolon.class);

        return new VariableDeclarationNode(isFinal, varName, typeNode, initializer);
    }

    private ASTNode parseRecordDeclaration() {
        // Expect a record identifier (record name)
        if (!typeEquals(RecordIdentifierType.class)) {
            throw new RuntimeException("Expected record name, but found: " + currentSymbol.type);
        }
        String recordName = currentSymbol.value;
        expect(RecordIdentifierType.class);

        // Expect 'rec' keyword
        expect(RecKeyword.class);

        // Expect '{'
        expect(LeftBrace.class);

        // Parse multiple variable declarations inside the record
        ArrayList<VariableDeclarationNode> fields = new ArrayList<>();
        while (!typeEquals(RightBrace.class)) {
            fields.add((VariableDeclarationNode) parseVariableDeclaration());
        }

        // Expect '}'
        expect(RightBrace.class);

        return new RecordDeclarationNode(recordName, fields);
    }

    private ASTNode parseFunctionDeclaration() {
        // Expect 'fun' keyword
        expect(FunKeyword.class);

        // Expect identifier (function name)
        if (!typeEquals(IdentifierType.class)) {
            throw new RuntimeException("Expected function name, but found: " + currentSymbol.type);
        }
        String functionName = currentSymbol.value;
        expect(IdentifierType.class);

        // Expect '('
        expect(LeftParenthesis.class);

        // Parse optional parameter list
        ArrayList<ParameterNode> parameters = new ArrayList<>();
        if (!typeEquals(RightParenthesis.class)) {
            parameters = parseParameterList();
        }

        // Expect ')'
        expect(RightParenthesis.class);

        // Parse optional return type
        ASTNode returnType = null;
        if (!typeEquals(LeftBrace.class)) { // If it's not a block, expect a return type
            returnType = parseType();
        }

        // Parse function body (Block)
        ASTNode functionBody = parseBlock();

        return new FunctionDeclarationNode(functionName, parameters, returnType, functionBody);
    }

    // Parse parameter list of function
    private ArrayList<ParameterNode> parseParameterList() {
        ArrayList<ParameterNode> parameters = new ArrayList<>();
        parameters.add(parseParameter());

        while (typeEquals(Comma.class)) {
            expect(Comma.class);
            parameters.add(parseParameter());
        }

        return parameters;
    }

    // Parse a parameter of a parameters list
    private ParameterNode parseParameter() {
        // Expect identifier (parameter name)
        if (!typeEquals(IdentifierType.class)) {
            throw new RuntimeException("Expected parameter name, but found: " + currentSymbol.type);
        }
        String paramName = currentSymbol.value;
        expect(IdentifierType.class);

        // Expect type
        ASTNode typeNode = parseType();

        return new ParameterNode(paramName, typeNode);
    }

    // Parses all possible statements, which is everything that can
    // be in the block of a function
    private ASTNode parseStatement() {
        if (typeEquals(IdentifierType.class)) {
            // Could be a variable declaration, assignment or function call
            return parseVariableDeclarationVariableAssignmentOrFunctionCall();
        }
        else if (typeEquals(IfKeyword.class)) {
            return parseIfStatement();
        }
        else if (typeEquals(WhileKeyword.class)) {
            return parseWhileStatement();
        }
        else if (typeEquals(ForKeyword.class)) {
            return parseForStatement();
        }
        else if (typeEquals(ReturnKeyword.class)) {
            return parseReturnStatement();
        }
        else if (typeEquals(LeftBrace.class)) {
            return parseBlock();
        }
        else if (typeEquals(FreeKeyword.class)) {
            return parseDeallocationStatement();
        }
        else {
            throw new RuntimeException("Unexpected statement: " + currentSymbol.type);
        }
    }

    private ASTNode parseVariableDeclarationVariableAssignmentOrFunctionCall() {
        // Read the first identifier
        ASTNode identifier = new IdentifierNode(currentSymbol.value);
        expect(IdentifierType.class);

        // Check if it's a variable declaration (next token is a type)
        if (typeEquals(BasicType.class) || typeEquals(RecordIdentifierType.class)) {
            // if yes, we need to backtrack so that the parser
            // can read the identifier again
            backtrack();
            return parseVariableDeclaration();
        }

        // Check if it's a function call
        else if (typeEquals(LeftParenthesis.class)) {
            ASTNode result = parseFunctionCall(identifier);
            // Expect ';'
            expect(Semicolon.class);
            return result;
        }

        // Last case, it's an assignment
        else {
            return parseAssignment(identifier);
        }
    }

    private ASTNode parseAssignment(ASTNode identifier) {
        ASTNode left = identifier;

        // Field or array accesses (but not function)
        while (typeEquals(Dot.class) || typeEquals(LeftBracket.class)) {
            if (typeEquals(Dot.class)) {
                left = parseRecordAccess(left);
            }
            else if (typeEquals(LeftBracket.class)) {
                left = parseArrayAccess(left);
            }
        }

        // Expect '='
        expect(Assignment.class);

        // Parse right expression
        ASTNode right = parseExpression();

        // Expect ';'
        expect(Semicolon.class);

        return new AssignmentNode(left, right);
    }

    private ASTNode parseIfStatement() {
        expect(IfKeyword.class);

        // Expect '('
        expect(LeftParenthesis.class);
        ASTNode condition = parseExpression();
        expect(RightParenthesis.class);

        // Parse 'if' block
        ASTNode ifBlock = parseBlock();

        // Store all "else if" branches
        ArrayList<ElseIfBranchNode> elseIfBranches = new ArrayList<>();

        while (true) {
            if (typeEquals(ElseKeyword.class)) {
                expect(ElseKeyword.class);
            }
            else{
                break;
            }
            if (typeEquals(IfKeyword.class)) {
                expect(IfKeyword.class);
            }
            else{
                backtrack();
                break;
            }

            expect(LeftParenthesis.class);
            ASTNode elseIfCondition = parseExpression();
            expect(RightParenthesis.class);

            ASTNode elseIfBlock = parseBlock();
            elseIfBranches.add(new ElseIfBranchNode(elseIfCondition, elseIfBlock));
        }

        // Parse optional 'else' block
        ASTNode elseBlock = null;
        if (typeEquals(ElseKeyword.class)) {
            expect(ElseKeyword.class);
            elseBlock = parseBlock();
        }

        return new IfStatementNode(condition, ifBlock, elseIfBranches, elseBlock);
    }


    private ASTNode parseWhileStatement() {
        expect(WhileKeyword.class);

        // Expect '('
        expect(LeftParenthesis.class);
        ASTNode condition = parseExpression();
        expect(RightParenthesis.class);

        // Parse while block
        ASTNode body = parseBlock();

        return new WhileStatementNode(condition, body);
    }

    // A variable is a chain of identifiers, field access and
    // array access
    public ASTNode parseVariable() {
        if (!typeEquals(IdentifierType.class)) {
            throw new RuntimeException("Expected variable name, but found: " + currentSymbol.type);
        }
        ASTNode left = new IdentifierNode(currentSymbol.value);
        expect(IdentifierType.class);
        // Field or array accesses
        while (typeEquals(Dot.class) || typeEquals(LeftBracket.class)) {
            if (typeEquals(Dot.class)) {
                left = parseRecordAccess(left);
            }
            else if (typeEquals(LeftBracket.class)) {
                left = parseArrayAccess(left);
            }
        }
        return new VariableNode(left);
    }

    private ASTNode parseForStatement() {
        expect(ForKeyword.class);

        // Expect '('
        expect(LeftParenthesis.class);

        // Parse initialization (variable)
        ASTNode variable = parseVariable();

        // Expect ','
        expect(Comma.class);

        // Parse initial value
        ASTNode initialValue = parseExpression();

        // Expect ','
        expect(Comma.class);

        // Parse max value
        ASTNode maxValue = parseExpression();

        expect(Comma.class);

        // Parse increment value
        ASTNode incrementValue = parseExpression();

        // Expect ')'
        expect(RightParenthesis.class);

        // Parse loop body
        ASTNode body = parseBlock();

        return new ForStatementNode(variable, initialValue, maxValue, incrementValue, body);
    }


    private ASTNode parseReturnStatement() {
        expect(ReturnKeyword.class);

        ASTNode returnValue = null;
        if (!typeEquals(Semicolon.class)) {
            returnValue = parseExpression();
        }

        // Expect ';'
        expect(Semicolon.class);

        return new ReturnStatementNode(returnValue);
    }


    // Parses a block { ... }
    private ASTNode parseBlock() {
        expect(LeftBrace.class);
        ArrayList<ASTNode> statements = new ArrayList<>();

        while (!typeEquals(RightBrace.class)) {
            statements.add(parseStatement());
        }

        expect(RightBrace.class);

        return new BlockNode(statements);
    }


    // Parses a 'free variable'
    private ASTNode parseDeallocationStatement() {
        expect(FreeKeyword.class);

        // Expect variable name
        if (!typeEquals(IdentifierType.class)) {
            throw new RuntimeException("Expected variable name after 'free', but found: " + currentSymbol.type);
        }

        String variableName = currentSymbol.value;
        ASTNode node = new IdentifierNode(variableName);
        expect(IdentifierType.class);

        while (typeEquals(Dot.class) || typeEquals(LeftBracket.class)) {
            if (typeEquals(Dot.class)) {
                expect(Dot.class);
                node = parseRecordAccess(node);
            }
            else if (typeEquals(LeftBracket.class)) {
                expect(LeftBracket.class);
                node = parseArrayAccess(node);
            }
        }

        // Expect ';'
        expect(Semicolon.class);

        return new DeallocationNode(node);
    }
}

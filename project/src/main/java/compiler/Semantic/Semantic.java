package compiler.Semantic;

import compiler.Lexer.Lexer;
import compiler.Lexer.Symbols.Keywords.FalseKeyword;
import compiler.Lexer.Symbols.Keywords.TrueKeyword;
import compiler.Lexer.Symbols.Keywords.Types.BoolType;
import compiler.Lexer.Symbols.Keywords.Types.FloatType;
import compiler.Lexer.Symbols.Keywords.Types.IntType;
import compiler.Lexer.Symbols.Keywords.Types.StringType;
import compiler.Lexer.Symbols.Numbers.FloatNumber;
import compiler.Lexer.Symbols.Numbers.IntegerNumber;
import compiler.Lexer.Symbols.RecordIdentifierType;
import compiler.Lexer.Symbols.StringValue;
import compiler.Parser.CFG.*;
import compiler.Parser.Parser;

import java.beans.Expression;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Semantic {

    public final ProgramNode root;

    public Stack<HashMap<String, ASTNode>> scopes;
    public HashMap<String, HashMap<String, VariableDeclarationNode>> records;
    public TypeNode currentFunctionReturnedType = null;

    public Semantic(ProgramNode root) {
        this.root = root;
        scopes = new Stack<>();
        records = new HashMap<>();
    }

    // Test if the node is the same as the expected one
    public boolean typeEquals(Object node, Class<?> expectedType) {
        return expectedType.isInstance(node);
    }

    ///Scope Stack operations

    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    public void popScope() {
        scopes.pop();
    }

    public void addVariableToScope(String name, ASTNode node) {
        scopes.peek().put(name, node);
    }

    ///End Scope Stack operations

    ///Records operations

    public void addRecord(RecordDeclarationNode node) {
        HashMap<String, VariableDeclarationNode> fields = new HashMap<>();
        for (VariableDeclarationNode field : node.fields) {
            if (fields.containsKey(field.varName)) {
                throw new RuntimeException("RecordError");
            }
            fields.put(field.varName, field);
        }
        records.put(node.recordName, fields);
    }

    /// End Records operations

    ///Errors:
    public void testScopeError(String name) {
        if (scopes.peek().containsKey(name)) {
            throw new RuntimeException("ScopeError");
        }
    }

    public Object getMeaningOfIdentifier(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                return scopes.get(i).get(name);
            }
        }
        // The identifier is not declared
        throw new RuntimeException("ScopeError");
    }

    public void testRecordError(String name) {
        if (scopes.peek().containsKey(name)) {
            throw new RuntimeException("RecordError");
        }
    }

    public void testFunctionCallParameters(ArrayList<ParameterNode> expectedParameters, ArrayList<ASTNode> arguments) {
        if (expectedParameters.size() != arguments.size()) {
            throw new RuntimeException("ArgumentError");
        }
        for (int i = 0; i < expectedParameters.size(); i++) {
            expectExpressionToHaveType((ExpressionStatementNode) arguments.get(i), (TypeNode) expectedParameters.get(i).type, "ArgumentError");
        }
    }

    public void testConstructorParameters(ArrayList<VariableDeclarationNode> expectedParameters, ArrayList<ASTNode> arguments) {
        if (expectedParameters.size() != arguments.size()) {
            throw new RuntimeException("ArgumentError");
        }
        for (int i = 0; i < expectedParameters.size(); i++) {
            expectExpressionToHaveType((ExpressionStatementNode) arguments.get(i), (TypeNode) expectedParameters.get(i).typeNode, "ArgumentError");
        }
    }

    ///End Errors:

    ///Expect expression type to be a specific type or find type of expression

    public TypeNode findTypeOfBinaryOperation(BinaryOperationNode node) {
        // Get the types of the left and right expressions
        TypeNode leftType = findTypeOfExpression(node.left);
        TypeNode rightType = findTypeOfExpression(node.right);

        // Try each case of binary operator
        // Logical 'Or' or Logical 'And'
        if (Objects.equals(node.operator, "||") || Objects.equals(node.operator, "&&")) {
            TypeNode correctType = new TypeNode(new BoolType(), false);
            if (!leftType.equals(correctType) || !rightType.equals(correctType)) {
                throw new RuntimeException("OperatorError");
            }
            return correctType;
        }
        // Equality
        if (Objects.equals(node.operator, "==") || Objects.equals(node.operator, "!=")) {
            // If both are whether an array or a record, it compares the pointers, so it always works
            if ((leftType.isArray || typeEquals(leftType.type, RecordIdentifierType.class)) &&
                    (rightType.isArray || typeEquals(rightType.type, RecordIdentifierType.class))) {
                return new TypeNode(new BoolType(), false);
            }
            // If one is array and the other not, fail
            if (leftType.isArray != rightType.isArray) {
                throw new RuntimeException("OperatorError");
            }
            // Now we know both are not arrays
            // If one is 'int' and the other 'float', accept the operation
            if (typeEquals(leftType.type, IntType.class) && typeEquals(rightType.type, FloatType.class) ||
                    typeEquals(leftType.type, FloatType.class) && typeEquals(rightType.type, IntType.class)) {
                return new TypeNode(new BoolType(), false);
            }
            // For the rest, if the types are different deny the operation
            if (!Objects.equals(leftType, rightType)) {
                throw new RuntimeException("OperatorError");
            }
            return new TypeNode(new BoolType(), false);
        }
        // Comparison
        if (Objects.equals(node.operator, "<") || Objects.equals(node.operator, ">") ||
                Objects.equals(node.operator, "<=") || Objects.equals(node.operator, ">=")) {
            // If any of them is an array, a record, a boolean or a string, deny
            if (leftType.isArray || typeEquals(leftType.type, RecordIdentifierType.class) || typeEquals(leftType.type, StringType.class) || typeEquals(leftType.type, BoolType.class) ||
                    rightType.isArray || typeEquals(rightType.type, RecordIdentifierType.class) || typeEquals(rightType.type, StringType.class) || typeEquals(rightType.type, BoolType.class)) {
                throw new RuntimeException("OperatorError");
            }
            // Now they can only ben non-arrays integers or floats, so we always accept
            return new TypeNode(new BoolType(), false);
        }
        // Term (+)
        if (Objects.equals(node.operator, "+")) {
            // If any of them is an array, a record or a boolean, deny
            if (leftType.isArray || typeEquals(leftType.type, RecordIdentifierType.class) || typeEquals(leftType.type, BoolType.class) ||
                    rightType.isArray || typeEquals(rightType.type, RecordIdentifierType.class) || typeEquals(rightType.type, BoolType.class)) {
                throw new RuntimeException("OperatorError");
            }
            // Now they can only ben non-arrays integers, floats or strings
            // If one is 'int' and the other 'float', return a float
            if (typeEquals(leftType.type, IntType.class) && typeEquals(rightType.type, FloatType.class) ||
                    typeEquals(leftType.type, FloatType.class) && typeEquals(rightType.type, IntType.class)) {
                return new TypeNode(new FloatType(), false);
            }
            // Else, if they have the same type the result is the same type
            if (Objects.equals(leftType, rightType)) {
                return new TypeNode(leftType.type, false);
            }
            else {
                throw new RuntimeException("OperatorError");
            }
        }
        // Term(-) and Factor (*, /)
        if (Objects.equals(node.operator, "-") || Objects.equals(node.operator, "*") || Objects.equals(node.operator, "/")) {
            // If any of them is an array, a record, a boolean or a string, deny
            if (leftType.isArray || typeEquals(leftType.type, RecordIdentifierType.class) || typeEquals(leftType.type, StringType.class) || typeEquals(leftType.type, BoolType.class) ||
                    rightType.isArray || typeEquals(rightType.type, RecordIdentifierType.class) || typeEquals(rightType.type, StringType.class) || typeEquals(rightType.type, BoolType.class)) {
                throw new RuntimeException("OperatorError");
            }
            // Now they can only ben non-arrays integers or floats
            // If one is 'float', return a float
            if (typeEquals(leftType.type, FloatType.class) || typeEquals(rightType.type, FloatType.class)) {
                return new TypeNode(new FloatType(), false);
            }
            // Else, if they have the same type the result is the same type
            if (Objects.equals(leftType, rightType)) {
                return new TypeNode(leftType.type, false);
            }
            else {
                throw new RuntimeException("OperatorError");
            }
        }
        // Factor (%)
        if (Objects.equals(node.operator, "%")) {
            // If any of them is an array, a record, a boolean, a string or a float, deny
            if (leftType.isArray || typeEquals(leftType.type, RecordIdentifierType.class) || typeEquals(leftType.type, StringType.class) || typeEquals(leftType.type, BoolType.class) || typeEquals(leftType.type, FloatType.class) ||
                    rightType.isArray || typeEquals(rightType.type, RecordIdentifierType.class) || typeEquals(rightType.type, StringType.class) || typeEquals(rightType.type, BoolType.class) || typeEquals(rightType.type, FloatType.class)) {
                throw new RuntimeException("OperatorError");
            }
            // Now they can only ben non-arrays integers
            if (typeEquals(leftType.type, IntType.class) && typeEquals(rightType.type, IntType.class)) {
                return new TypeNode(new IntType(), false);
            }
            else {
                throw new RuntimeException("OperatorError");
            }
        }
        // Normally you can't get here
        throw new RuntimeException("Binary operator not recognized");
    }

    public TypeNode findTypeOfUnaryOperation(UnaryOperationNode node) {
        // Get the type of the expression
        TypeNode nodeType = findTypeOfExpression(node.node);

        // Try all possible unary operations
        // '!' operator
        if (Objects.equals(node.operator, "!")) {
            // Only works on boolean non-arrays expressions
            if (!nodeType.isArray && typeEquals(nodeType.type, BoolType.class)) {
                return new TypeNode(new BoolType(), false);
            }
            else {
                throw new RuntimeException("OperatorError");
            }
        }
        //  '-' operator
        if (Objects.equals(node.operator, "-")) {
            // Only works on int and float non-array expressions
            if (!nodeType.isArray && (typeEquals(nodeType.type, IntType.class) || typeEquals(nodeType.type, FloatType.class))) {
                return new TypeNode(nodeType.type, false);
            }
            else {
                throw new RuntimeException("OperatorError");
            }
        }
        // Normally you can't get here
        throw new RuntimeException("Unary operator not recognized");
    }

    public TypeNode findTypeOfLiteral(LiteralNode node) {
        // Integer
        if (typeEquals(node.literal, IntegerNumber.class)) {
            return new TypeNode(new IntType(), false);
        }
        // Float
        if (typeEquals(node.literal, FloatNumber.class)) {
            return new TypeNode(new FloatType(), false);
        }
        // String
        if (typeEquals(node.literal, StringValue.class)) {
            return new TypeNode(new StringType(), false);
        }
        // False
        if (typeEquals(node.literal, FalseKeyword.class)) {
            return new TypeNode(new BoolType(), false);
        }
        // True
        if (typeEquals(node.literal, TrueKeyword.class)) {
            return new TypeNode(new BoolType(), false);
        }
        // Normally you can't get here
        throw new RuntimeException("Literal not recognized");
    }

    public TypeNode findTypeOfArrayLiteral(ArrayLiteralNode node) {
        if (node.type.isArray) {
            // Array of arrays not allowed
            throw new RuntimeException("TypeError");
        }
        // Set the isArray to true but keep the main type
        return new TypeNode(node.type.type, true);
    }

    // This function normally only returns TypeNode objects, but return VariableDeclaration,
    // RecordDeclaration or FunctionDeclaration only when the node is an Identifier
    public Object findTypeOfVariable(ASTNode node) {
        // Base case
        if (typeEquals(node, ExpressionStatementNode.class)) {
            return findTypeOfExpression(((ExpressionStatementNode) node).expression);
        }
        if (typeEquals(node, IdentifierNode.class)) {
            IdentifierNode identifierNode = (IdentifierNode) node;
            // Can be VariableDeclaration, RecordDeclaration or FunctionDeclaration
            return getMeaningOfIdentifier(identifierNode.name);
        }
        // Function call (Can also be Record Constructor)
        if (typeEquals(node, FunctionCallNode.class)) {
            FunctionCallNode functionCallNode = (FunctionCallNode) node;
            Object meaningFunctionCallNode = findTypeOfVariable(functionCallNode.functionNode);
            if (typeEquals(meaningFunctionCallNode, FunctionDeclarationNode.class)) {
                FunctionDeclarationNode functionDeclarationNode = (FunctionDeclarationNode) meaningFunctionCallNode;
                // Have to check parameters
                testFunctionCallParameters(functionDeclarationNode.parameters, functionCallNode.arguments);
                // Return the return type of the function
                return functionDeclarationNode.returnType;
            }
            else if (typeEquals(meaningFunctionCallNode, RecordDeclarationNode.class)) {
                RecordDeclarationNode recordDeclarationNode = (RecordDeclarationNode) meaningFunctionCallNode;
                // Have to check parameters
                testConstructorParameters(recordDeclarationNode.fields, functionCallNode.arguments);
                // Return the type of the Record
                return new TypeNode(new RecordIdentifierType(recordDeclarationNode.recordName), false);
            }
            else {
                // The object is not callable
                throw new RuntimeException("OperatorError");
            }
        }
        // Array access
        if (typeEquals(node, ArrayAccessNode.class)) {
            ArrayAccessNode arrayAccessNode = (ArrayAccessNode) node;
            // Recursively find the meaning of the node array
            Object meaningArrayNode = findTypeOfVariable(arrayAccessNode.arrayNode);
            TypeNode nodeType;
            if (typeEquals(meaningArrayNode, VariableDeclarationNode.class)) {
                VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) meaningArrayNode;
                nodeType = variableDeclarationNode.typeNode;
            }
            else if (typeEquals(meaningArrayNode, TypeNode.class)) {
                nodeType = (TypeNode) meaningArrayNode;
            }
            else {
                // The variable can't be an array
                throw new RuntimeException("OperatorError");
            }
            if (nodeType.isArray) {
                // Check capacity is integer
                expectExpressionToHaveType((ExpressionStatementNode) arrayAccessNode.index, new TypeNode(new IntType(), false));
                return new TypeNode(nodeType.type, false);
            }
            else {
                // The variable is not an array
                throw new RuntimeException("OperatorError");
            }
        }
        // Record access
        if (typeEquals(node, RecordAccessNode.class)) {
            RecordAccessNode recordAccessNode = (RecordAccessNode) node;
            Object meaningRecordNode = findTypeOfVariable(recordAccessNode.recordNode);
            TypeNode nodeType;
            if (typeEquals(meaningRecordNode, VariableDeclarationNode.class)) {
                VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) meaningRecordNode;
                nodeType = variableDeclarationNode.typeNode;
            }
            else if (typeEquals(meaningRecordNode, TypeNode.class)) {
                nodeType = (TypeNode) meaningRecordNode;
            }
            else {
                // The variable can't be a record
                throw new RuntimeException("OperatorError");
            }
            // Check the type is a record and exists and if the field of that record exists
            if (records.containsKey(nodeType.type.value) && records.get(nodeType.type.value).containsKey(recordAccessNode.name)) {
                // Return the type of that field
                return records.get(nodeType.type.value).get(recordAccessNode.name).typeNode;
            }
            else {
                // The variable isn't a record
                throw new RuntimeException("OperatorError");
            }
        }
        throw new RuntimeException("Variable not recognized");
    }

    public TypeNode findTypeOfPrimaryExpression(ASTNode node) {
        // If literal
        if (typeEquals(node, LiteralNode.class)) {
            return findTypeOfLiteral((LiteralNode) node);
        }
        // If Array Literal
        if (typeEquals(node, ArrayLiteralNode.class)) {
            return findTypeOfArrayLiteral((ArrayLiteralNode) node);
        }
        // If complex variable (field access, array access, function call, variable etc.)
        Object meaning = findTypeOfVariable(node);
        TypeNode meaningType;
        if (typeEquals(meaning, VariableDeclarationNode.class)) {
            VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) meaning;
            meaningType = variableDeclarationNode.typeNode;
        }
        else if (typeEquals(meaning, TypeNode.class)) {
            meaningType = (TypeNode) meaning;
        }
        else {
            throw new RuntimeException("OperatorError");
        }
        return meaningType;
    }

    public TypeNode findTypeOfExpression(ASTNode node) {
        // Binary operator
        if (typeEquals(node, BinaryOperationNode.class)) {
            return findTypeOfBinaryOperation((BinaryOperationNode) node);
        }
        // Unary operator
        else if (typeEquals(node, UnaryOperationNode.class)) {
            return findTypeOfUnaryOperation((UnaryOperationNode) node);
        }
        // Primary expression
        else {
            return findTypeOfPrimaryExpression(node);
        }
    }

    public boolean leftIsFloatAndRightIsInt(TypeNode leftType, TypeNode rightType) {
        // Return True only when both types are not arrays and left is float and right is int
        return Objects.equals(leftType.type.value, "float") && Objects.equals(rightType.type.value, "int") && !leftType.isArray && !rightType.isArray;
    }

    public void expectExpressionToHaveType(ExpressionStatementNode node, TypeNode expectedType) {
        TypeNode expressionType = findTypeOfExpression(node.expression);
        if (!Objects.equals(expressionType, expectedType) && !leftIsFloatAndRightIsInt(expectedType, expressionType)) {
            throw new RuntimeException("TypeError");
        }
    }

    public void expectExpressionToHaveType(ExpressionStatementNode node, TypeNode expectedType, String error) {
        TypeNode expressionType = findTypeOfExpression(node.expression);
        if (!Objects.equals(expressionType, expectedType) && !leftIsFloatAndRightIsInt(expectedType, expressionType)) {
            throw new RuntimeException(error);
        }
    }

    ///End expect expression type to be a specific type or find type of expression

    ///Add functions names to global scope

    public void addBuiltInFunctions(String filePathString) throws IOException {
        Path filePath = Paths.get(filePathString);
        String content = Files.readString(filePath);
        Lexer lexer = new Lexer(new StringReader(content));
        Parser parser = new Parser(lexer);
        ASTNode root = parser.getAST();
        addAllFunctionDeclarationsToScope((ProgramNode) root);
    }

    public void addAllFunctionDeclarationsToScope(ProgramNode root) {
        // All the function declarations are in the global scope
        for (ASTNode node : root.nodes) {
            // Check the node is a function declaration
            if (typeEquals(node, FunctionDeclarationNode.class)) {
                FunctionDeclarationNode functionDeclarationNode = (FunctionDeclarationNode) node;
                // Test the scope error (we don't want 2 functions with the same name)
                testScopeError(functionDeclarationNode.functionName);
                // Add the function to the scope
                addVariableToScope(functionDeclarationNode.functionName, functionDeclarationNode);
            }
        }
    }

    ///End add functions names to global scope

    /// Main function (entry point)
    public void findSemanticErrors() throws IOException {
        // Create initial global scope
        pushScope();
        // Add to global scope all function declarations, including built-in ones
        addAllFunctionDeclarationsToScope(root);
        // Now we add the built-in function in the "built_in_functions.txt" file
        addBuiltInFunctions("src/main/java/compiler/Semantic/built_in_functions.txt");
        //
        programChildrenErrors();
    }

    ///Check record declaration

    public void checkRecordDeclaration(RecordDeclarationNode node) {
        // Check record name is unique
        testRecordError(node.recordName);
        // Add the record name to the scope so other variables can't have its name
        addVariableToScope(node.recordName, node);
        // Add the record to the `records` map
        addRecord(node);
    }

    ///End check record declaration

    ///Check all statements

    public void checkAssignment(AssignmentNode node) {
        //Object meaning = getMeaningOfIdentifier(node.left)
    }

    ///End check all statements

    ///Check function declaration

    public void checkBlockNode(BlockNode node) {
        pushScope();
        for (ASTNode statement : node.statements) {
            // Variable declaration
            if (typeEquals(statement, VariableDeclarationNode.class)) {
                checkVariableDeclaration((VariableDeclarationNode) statement);
            }
            // Assignment
            else if (typeEquals(statement, AssignmentNode.class)) {
                AssignmentNode assignmentNode = (AssignmentNode) statement;
                TypeNode leftType = findTypeOfPrimaryExpression(assignmentNode.left);
                expectExpressionToHaveType((ExpressionStatementNode) assignmentNode.right, leftType);
            }
            // Block
            else if (typeEquals(statement, BlockNode.class)) {
                checkBlockNode((BlockNode) statement);
            }
            // If
            else if (typeEquals(statement, IfStatementNode.class)) {
                IfStatementNode ifStatementNode = (IfStatementNode) statement;
                TypeNode conditionType = findTypeOfExpression(((ExpressionStatementNode)ifStatementNode.condition).expression);
                if (!Objects.equals(conditionType, new TypeNode(new BoolType(), false))) {
                    throw new RuntimeException("MissingConditionError");
                }
                checkBlockNode((BlockNode) ifStatementNode.ifBlock);
                for (ElseIfBranchNode elseIfBranchNode : ifStatementNode.elseIfBranches) {
                    conditionType = findTypeOfExpression(elseIfBranchNode.condition);
                    if (!Objects.equals(conditionType, new TypeNode(new BoolType(), false))) {
                        throw new RuntimeException("MissingConditionError");
                    }
                    checkBlockNode((BlockNode) elseIfBranchNode.block);
                }
                if (ifStatementNode.elseBlock != null) {
                    checkBlockNode((BlockNode) ifStatementNode.elseBlock);
                }
            }
            // While
            else if (typeEquals(statement, WhileStatementNode.class)) {
                WhileStatementNode whileStatementNode = (WhileStatementNode) statement;
                TypeNode conditionType = findTypeOfExpression(((ExpressionStatementNode)whileStatementNode.condition).expression);
                if (!Objects.equals(conditionType, new TypeNode(new BoolType(), false))) {
                    throw new RuntimeException("MissingConditionError");
                }
                checkBlockNode((BlockNode) whileStatementNode.body);
            }
            // For
            else if (typeEquals(statement, ForStatementNode.class)) {
                ForStatementNode forStatementNode = (ForStatementNode) statement;
                expectExpressionToHaveType(new ExpressionStatementNode(forStatementNode.variable), new TypeNode(new FloatType(), false));
                expectExpressionToHaveType((ExpressionStatementNode) forStatementNode.initialValue, new TypeNode(new FloatType(), false));
                expectExpressionToHaveType((ExpressionStatementNode) forStatementNode.maxValue, new TypeNode(new FloatType(), false));
                expectExpressionToHaveType((ExpressionStatementNode) forStatementNode.incrementValue, new TypeNode(new FloatType(), false));
                checkBlockNode((BlockNode) forStatementNode.body);
            }
            // Function call
            else if (typeEquals(statement, FunctionCallNode.class)) {
                FunctionCallNode functionCallNode = (FunctionCallNode) statement;
                // Here is the problem
                System.out.println(statement);
                findTypeOfVariable(functionCallNode);
            }
            // Free
            else if (typeEquals(statement, DeallocationNode.class)) {
                DeallocationNode deallocationNode = (DeallocationNode) statement;
                TypeNode variableType = findTypeOfPrimaryExpression(deallocationNode.variable);
                // Can add Free error if not array or record
            }
            // Return
            else if (typeEquals(statement, ReturnStatementNode.class)) {
                ReturnStatementNode returnStatementNode = (ReturnStatementNode) statement;
                ExpressionStatementNode returnExpression = (ExpressionStatementNode) returnStatementNode.value;
                TypeNode returnedType = findTypeOfExpression(returnExpression.expression);
                if (currentFunctionReturnedType == null) {
                    currentFunctionReturnedType = returnedType;
                }
            }
            else {
                throw new RuntimeException("Statement unrecognized");
            }
        }
        popScope();
    }

    public void checkFunctionDeclaration(FunctionDeclarationNode node) {
        // Add a scope for the function
        pushScope();
        // Set the returned type to null
        currentFunctionReturnedType = null;
        // Add all parameters to scope
        for (ParameterNode parameter : node.parameters) {
            // Make the Param a Variable Declaration so it's easier to manage the scope objects
            addVariableToScope(parameter.paramName, new VariableDeclarationNode(false, parameter.paramName, (TypeNode) parameter.type, null));
        }
        BlockNode functionBlock = (BlockNode) node.functionBody;
        checkBlockNode(functionBlock);
        TypeNode functionReturnType = (TypeNode) node.returnType;
        // If one is null and the other not, throw error
        if ((currentFunctionReturnedType == null && functionReturnType != null) || (currentFunctionReturnedType != null && functionReturnType == null)) {
            throw new RuntimeException("ReturnError");
        }
        // If both are not null but have different types, throw error
        if (currentFunctionReturnedType != null) {
            if (!Objects.equals(currentFunctionReturnedType, functionReturnType)) {
                throw new RuntimeException("ReturnError");
            }
        }

        // Pop the scope when exiting function
        popScope();
    }

    ///End check function declaration

    ///Check variable declaration

    public void checkRecordTypeVariableDeclaration(VariableDeclarationNode node) {
        // In case the type is a Record, if the Record doesn't exist, throw type error
        if (typeEquals(node.typeNode.type, RecordIdentifierType.class) && !records.containsKey(node.typeNode.type.value)) {
            throw new RuntimeException("TypeError");
        }
    }

    public void checkExpectedTypeOfInitialisation(VariableDeclarationNode node) {
        // If the variable is assigned
        if (node.initializer != null) {
            expectExpressionToHaveType((ExpressionStatementNode) node.initializer, node.typeNode);
        }
    }

    public void checkVariableDeclaration(VariableDeclarationNode node) {
        // Check variable name is unique
        testScopeError(node.varName);
        // Add the variable name to the scope
        addVariableToScope(node.varName, node);
        // Check if type is Record and if it's already declared
        checkRecordTypeVariableDeclaration(node);
        // Expect the type of initialisation value or the array capacity
        checkExpectedTypeOfInitialisation(node);
    }

    ///End check variable declaration

    public void programChildrenErrors() {
        for (ASTNode node : root.nodes) {
            if (typeEquals(node, VariableDeclarationNode.class)) {
                checkVariableDeclaration((VariableDeclarationNode) node);
            }
            else if (typeEquals(node, FunctionDeclarationNode.class)) {
                checkFunctionDeclaration((FunctionDeclarationNode) node);
            }
            else if (typeEquals(node, RecordDeclarationNode.class)) {
                checkRecordDeclaration((RecordDeclarationNode) node);
            }
        }
    }
}
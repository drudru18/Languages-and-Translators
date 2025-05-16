package compiler.CodeGeneration;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public class CodeGeneration {

    public class NodeAndIndex {
        ASTNode node;
        int index;

        public NodeAndIndex(ASTNode node, int index) {
            this.node = node;
            this.index = index;
        }
    }

    private static final HashSet<String> flexibleFunctions = new HashSet<>();
    static {
        flexibleFunctions.add("write");
        flexibleFunctions.add("writeln");
    }

    // Be careful to increase and decrease this value when necessary
    private static int currentVarIndex = 0;

    public class MapAndCurrentIndex {
        HashMap<String, NodeAndIndex> map;
         int variablesDeclaredInScope;

        public MapAndCurrentIndex() {
            this.map = new HashMap<>();
            int variablesDeclaredInScope = 0;
        }
    }

    public final ProgramNode root;

    public Stack<MapAndCurrentIndex> scopes;
    public HashMap<String, HashMap<String, VariableDeclarationNode>> records;
    public TypeNode currentFunctionReturnedType = null;

    /// Code type to Bytecode type notation

    private static final HashMap<String, String> typesInBytecode = new HashMap<>();
    static {
        typesInBytecode.put("int", "I");
        typesInBytecode.put("float", "F");
        typesInBytecode.put("bool", "Z");
        typesInBytecode.put("string", "Ljava/lang/String;");
    }

    // Returns the correct bytecode string type
    private String getTypeOfVariableInBytecode(TypeNode type) {
        String rawType = type.type.value;
        // If first letter is lower case => it's primitive type, so get the Bytecode name
        if (rawType.charAt(0) >= 'a' && rawType.charAt(0) <= 'z') {
            rawType = typesInBytecode.get(rawType);
        }
        // Else, it means it's record, so put "L" and ";" at left and right
        else {
            rawType = "L" + rawType + ";";
        }
        if (type.isArray) {
            rawType = "[" + rawType;
        }
        return rawType;
    }

    /// Type to Load Opcode

    private static final HashMap<String, Integer> typeToLoadOpcode = new HashMap<>();
    static {
        typeToLoadOpcode.put("int", ILOAD);
        typeToLoadOpcode.put("float", FLOAD);
        typeToLoadOpcode.put("bool", ILOAD);
        typeToLoadOpcode.put("string", ALOAD);
    }

    private int getTypeToLoadOpcode(TypeNode typeNode) {
        String typeName = typeNode.type.value;
        if (typeNode.isArray || !typeToLoadOpcode.containsKey(typeName)) {
            return ALOAD;
        }
        return typeToLoadOpcode.get(typeName);
    }

    /// Array Type to Load Opcode

    private static final HashMap<String, Integer> arrayTypeToLoadOpcode = new HashMap<>();
    static {
        arrayTypeToLoadOpcode.put("int", IALOAD);
        arrayTypeToLoadOpcode.put("float", FALOAD);
        arrayTypeToLoadOpcode.put("bool", BALOAD);
        arrayTypeToLoadOpcode.put("string", AALOAD);
    }

    private int getArrayTypeToLoadOpcode(TypeNode typeNode) {
        String typeName = typeNode.type.value;
        if (typeNode.isArray || !arrayTypeToLoadOpcode.containsKey(typeName)) {
            return AALOAD;
        }
        return arrayTypeToLoadOpcode.get(typeName);
    }

    /// Type to Store Opcode

    private static final HashMap<String, Integer> typeToStoreOpcode = new HashMap<>();
    static {
        typeToStoreOpcode.put("int", ISTORE);
        typeToStoreOpcode.put("float", FSTORE);
        typeToStoreOpcode.put("bool", ISTORE);
        typeToStoreOpcode.put("string", ASTORE);
    }

    private int getTypeToStoreOpcode(TypeNode typeNode) {
        String typeName = typeNode.type.value;
        if (typeNode.isArray || !typeToStoreOpcode.containsKey(typeName)) {
            return ASTORE;
        }
        return typeToStoreOpcode.get(typeName);
    }

    /// Array Type to Store Opcode

    private static final HashMap<String, Integer> arrayTypeToStoreOpcode = new HashMap<>();
    static {
        arrayTypeToStoreOpcode.put("int", IASTORE);
        arrayTypeToStoreOpcode.put("float", FASTORE);
        arrayTypeToStoreOpcode.put("bool", BASTORE);
        arrayTypeToStoreOpcode.put("string", AASTORE);
    }

    private int getArrayTypeToStoreOpcode(TypeNode typeNode) {
        String typeName = typeNode.type.value;
        if (typeNode.isArray || !arrayTypeToStoreOpcode.containsKey(typeName)) {
            return AASTORE;
        }
        return arrayTypeToStoreOpcode.get(typeName);
    }

    /// Opcode for Return

    private static final HashMap<String, Integer> typeForReturnBytecode = new HashMap<>();
    static {
        typeForReturnBytecode.put("int", IRETURN);
        typeForReturnBytecode.put("float", FRETURN);
        typeForReturnBytecode.put("bool", IRETURN);
        typeForReturnBytecode.put("string", ARETURN);
    }

    private int getTypeForReturnBytecode(TypeNode typeNode) {
        String typeName = typeNode.type.value;
        if (typeNode.isArray || !typeForReturnBytecode.containsKey(typeName)) {
            return ARETURN;
        }
        return typeForReturnBytecode.get(typeName);
    }

    /// Type to array type

    private static final HashMap<String, Integer> typeToArrayType = new HashMap<>();
    static {
        typeToArrayType.put("int", T_INT);
        typeToArrayType.put("float", T_FLOAT);
        typeToArrayType.put("bool", T_BOOLEAN);
    }

    ///

    private ArrayList<VariableDeclarationNode> initialisedGlobalVariables;

    private String outputFolderAndFileName;
    private String outputFolderName;
    private String outputFileName;
    private String mainClassName;

    private final int javaV = V17;

    public CodeGeneration(ProgramNode root, String outputFolderAndFileName) {
        this.root = root;
        scopes = new Stack<>();
        records = new HashMap<>();

        this.outputFolderAndFileName = outputFolderAndFileName;
        initialisedGlobalVariables = new ArrayList<>();
    }

    public class VariableMeaning {

        NodeAndIndex meaning;
        Boolean isGlobal;

        public VariableMeaning(NodeAndIndex meaning, Boolean isGlobal) {
            this.meaning = meaning;
            this.isGlobal = isGlobal;
        }
    }

    // Test if the node is the same as the expected one
    public boolean typeEquals(Object node, Class<?> expectedType) {
        return expectedType.isInstance(node);
    }

    ///Scope Stack operations

    public void pushScope() {
        scopes.push(new MapAndCurrentIndex());
    }

    public void popScope() {
        currentVarIndex -= scopes.peek().variablesDeclaredInScope;
        scopes.pop();
    }

    public void addVariableToScope(String name, ASTNode node) {
        scopes.peek().map.put(name, new NodeAndIndex(node, currentVarIndex));
        // only increase `variablesDeclaredInScope` when the scope is a local one
        if (scopes.size() > 1) {
            scopes.peek().variablesDeclaredInScope++;
            currentVarIndex++;
        }
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
        if (scopes.peek().map.containsKey(name)) {
            throw new RuntimeException("ScopeError");
        }
    }

    // Returns the meaning and true if the variable is global
    public VariableMeaning getMeaningOfIdentifier(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).map.containsKey(name)) {
                return new VariableMeaning(scopes.get(i).map.get(name), i == 0);
            }
        }
        // Normally never getting here
        return null;
    }

    public void testRecordError(String name) {
        if (scopes.peek().map.containsKey(name)) {
            throw new RuntimeException("RecordError");
        }
    }

    public void testFunctionCallParameters(ArrayList<ParameterNode> expectedParameters, ArrayList<ASTNode> arguments) {

    }

    public void testConstructorParameters(ArrayList<VariableDeclarationNode> expectedParameters, ArrayList<ASTNode> arguments) {
        if (expectedParameters.size() != arguments.size()) {
            throw new RuntimeException("ArgumentError");
        }
        for (int i = 0; i < expectedParameters.size(); i++) {
            expectExpressionToHaveType((ExpressionStatementNode) arguments.get(i), (TypeNode) expectedParameters.get(i).typeNode, "ArgumentError");
        }
    }

    /// Generate code for binary operations

    private TypeNode makeFloatTypesIfOneIsFloat(TypeNode leftType, TypeNode rightType) {
        if (Objects.equals(leftType.type.value, "float") && Objects.equals(rightType.type.value, "int")) {
            return leftType;
        }
        else if (Objects.equals(leftType.type.value, "int") && Objects.equals(rightType.type.value, "float")) {
            return rightType;
        }
        return leftType;
    }

    private boolean compareReference(TypeNode typeNode) {
        return typeNode.isArray || Objects.equals(typeNode.type.value, "string") ||
                typeNode.type.value.charAt(0) >= 'A' && typeNode.type.value.charAt(0) <= 'Z';
    }

    private void generateCodeForOr(MethodVisitor mv, BinaryOperationNode node) {
        // apply logical or
        mv.visitInsn(IOR);
    }

    private void generateCodeForAnd(MethodVisitor mv, BinaryOperationNode node) {
        // apply logical or
        mv.visitInsn(IAND);
    }

    private void generateCodeForEquality(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode, String op) {
        int first_val = ICONST_0;
        int second_val = ICONST_1;
        if (Objects.equals(op, "!=")) {
            int temp = first_val;
            first_val = second_val;
            second_val = temp;
        }

        // apply `==` or `!=`
        // create labels
        Label labelTrue = new Label();
        Label labelEnd = new Label();

        // reference comparison
        if (compareReference(typeNode)) {
            mv.visitJumpInsn(IF_ACMPEQ, labelTrue);
        }
        // float comparison
        else if (Objects.equals(typeNode.type.value, "float")) {
            mv.visitInsn(FCMPL);
            mv.visitJumpInsn(IFEQ, labelTrue);
        }
        // int comparison
        else if (Objects.equals(typeNode.type.value, "int") || Objects.equals(typeNode.type.value, "bool")) {
            mv.visitJumpInsn(IF_ICMPEQ, labelTrue);
        }
        // else, push 0 (false)
        mv.visitInsn(first_val);
        // and then go to End Label
        mv.visitJumpInsn(GOTO, labelEnd);

        // left == right case
        mv.visitLabel(labelTrue);
        // push 1 (true)
        mv.visitInsn(second_val);

        mv.visitLabel(labelEnd);
    }

    private void generateCodeForLessThan(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode, String op) {
        // create labels
        Label labelTrue = new Label();
        Label labelEnd = new Label();

        // float comparison
        if (Objects.equals(typeNode.type.value, "float")) {
            mv.visitInsn(FCMPL);
            mv.visitJumpInsn(IFLT, labelTrue);
        }
        // int comparison
        else if (Objects.equals(typeNode.type.value, "int")) {
            mv.visitJumpInsn(IF_ICMPLT, labelTrue);
        }
        // else, push 0 (false)
        mv.visitInsn(ICONST_0);
        // and then go to End Label
        mv.visitJumpInsn(GOTO, labelEnd);

        // left < right case
        mv.visitLabel(labelTrue);
        // push 1 (true)
        mv.visitInsn(ICONST_1);

        mv.visitLabel(labelEnd);
    }

    private void generateCodeForGreaterOrEqual(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode, String op) {
        // create labels
        Label labelTrue = new Label();
        Label labelEnd = new Label();

        // float comparison
        if (Objects.equals(typeNode.type.value, "float")) {
            mv.visitInsn(FCMPL);
            mv.visitJumpInsn(IFGE, labelTrue);
        }
        // int comparison
        else if (Objects.equals(typeNode.type.value, "int")) {
            mv.visitJumpInsn(IF_ICMPGE, labelTrue);
        }
        // else, push 0 (false)
        mv.visitInsn(ICONST_0);
        // and then go to End Label
        mv.visitJumpInsn(GOTO, labelEnd);

        // left < right case
        mv.visitLabel(labelTrue);
        // push 1 (true)
        mv.visitInsn(ICONST_1);

        mv.visitLabel(labelEnd);
    }

    private void generateCodeForGreaterThan(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode, String op) {
        // create labels
        Label labelTrue = new Label();
        Label labelEnd = new Label();

        // float comparison
        if (Objects.equals(typeNode.type.value, "float")) {
            mv.visitInsn(FCMPL);
            mv.visitJumpInsn(IFGT, labelTrue);
        }
        // int comparison
        else if (Objects.equals(typeNode.type.value, "int")) {
            mv.visitJumpInsn(IF_ICMPGT, labelTrue);
        }
        // else, push 0 (false)
        mv.visitInsn(ICONST_0);
        // and then go to End Label
        mv.visitJumpInsn(GOTO, labelEnd);

        // left < right case
        mv.visitLabel(labelTrue);
        // push 1 (true)
        mv.visitInsn(ICONST_1);

        mv.visitLabel(labelEnd);
    }

    private void generateCodeForLessOrEqual(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode, String op) {
        // create labels
        Label labelTrue = new Label();
        Label labelEnd = new Label();

        // float comparison
        if (Objects.equals(typeNode.type.value, "float")) {
            mv.visitInsn(FCMPL);
            mv.visitJumpInsn(IFLE, labelTrue);
        }
        // int comparison
        else if (Objects.equals(typeNode.type.value, "int")) {
            mv.visitJumpInsn(IF_ICMPLE, labelTrue);
        }
        // else, push 0 (false)
        mv.visitInsn(ICONST_0);
        // and then go to End Label
        mv.visitJumpInsn(GOTO, labelEnd);

        // left < right case
        mv.visitLabel(labelTrue);
        // push 1 (true)
        mv.visitInsn(ICONST_1);

        mv.visitLabel(labelEnd);
    }

    private void generateCodeForSum(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode) {

        switch (typeNode.type.value) {
            case "int":
                mv.visitInsn(IADD);
                break;
            case "float":
                mv.visitInsn(FADD);
                break;
            case "string":
                // Currently, the 2 strings are in this order on the stack:
                // string1 -> string2 (top). First we swap them
                mv.visitInsn(SWAP);
                // Now: string2 -> string1
                // Create new StringBuilder
                mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                // Now: string2 -> string1 -> StringBuilder

                // Append first string
                mv.visitInsn(SWAP); // Swap the first string and StringBuilder
                // Now: string2 -> StringBuilder -> string1
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

                // Now: string2 -> StringBuilder
                // Append second string
                mv.visitInsn(SWAP); // Swap the second string and StringBuilder
                // Now StringBuilder -> string2
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                // Now StringBuilder
                // Convert to String
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
                        "()Ljava/lang/String;", false);
                break;
        }
    }

    private void generateCodeForSubtraction(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode) {

        switch (typeNode.type.value) {
            case "int":
                mv.visitInsn(ISUB);
                break;
            case "float":
                mv.visitInsn(FSUB);
                break;
        }
    }

    private void generateCodeForMultiplication(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode) {

        switch (typeNode.type.value) {
            case "int":
                mv.visitInsn(IMUL);
                break;
            case "float":
                mv.visitInsn(FMUL);
                break;
        }
    }

    private void generateCodeForDivision(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode) {

        switch (typeNode.type.value) {
            case "int":
                mv.visitInsn(IDIV);
                break;
            case "float":
                mv.visitInsn(FDIV);
                break;
        }
    }

    private void generateCodeForModulo(MethodVisitor mv, BinaryOperationNode node, TypeNode typeNode) {

        switch (typeNode.type.value) {
            case "int":
                mv.visitInsn(IREM);
                break;
            case "float":
                mv.visitInsn(FREM);
                break;
        }
    }

    private void generateCodeForBinaryOperation(MethodVisitor mv, BinaryOperationNode node) {
        // Get the type of the left expression and right
        TypeNode leftType = findTypeOfExpression(node.left);
        TypeNode rightType = findTypeOfExpression(node.right);

        // Generate left expression
        codeGenerationExpression(mv, node.left);

        // if left is int and right is float, convert left to float
        if (Objects.equals(leftType.type.value, "int") && Objects.equals(rightType.type.value, "float")) {
            mv.visitInsn(I2F);
        }

        // generate right expression
        codeGenerationExpression(mv, node.right);

        // if left is float and right is int convert right to float
        if (Objects.equals(leftType.type.value, "float") && Objects.equals(rightType.type.value, "int")) {
            mv.visitInsn(I2F);
        }

        // change expressions types to float if at least 1 is float
        TypeNode correctType = makeFloatTypesIfOneIsFloat(leftType, rightType);

        switch (node.operator) {
            case "||":
                generateCodeForOr(mv, node);
                break;
            case "&&":
                generateCodeForAnd(mv, node);
                break;
            case "==":
            case "!=":
                generateCodeForEquality(mv, node, correctType, node.operator);
                break;
            case "<":
                generateCodeForLessThan(mv, node, correctType, node.operator);
                break;
            case ">":
                generateCodeForGreaterThan(mv, node, correctType, node.operator);
                break;
            case "<=":
                generateCodeForLessOrEqual(mv, node, correctType, node.operator);
                break;
            case ">=":
                generateCodeForGreaterOrEqual(mv, node, correctType, node.operator);
                break;
            case "+":
                generateCodeForSum(mv, node, correctType);
                break;
            case "-":
                generateCodeForSubtraction(mv, node, correctType);
                break;
            case "*":
                generateCodeForMultiplication(mv, node, correctType);
                break;
            case "/":
                generateCodeForDivision(mv, node, correctType);
                break;
            case "%":
                generateCodeForModulo(mv, node, correctType);
                break;
        }
    }

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

    /// Generate code for unary operations

    private void generateCodeForUnaryMinus(MethodVisitor mv, UnaryOperationNode node, TypeNode typeNode) {
        switch (typeNode.type.value) {
            case "int":
                mv.visitInsn(INEG);
                break;
            case "float":
                mv.visitInsn(FNEG);
                break;
        }
    }

    private void generateCodeForNon(MethodVisitor mv, UnaryOperationNode node) {
        Label labelTrue = new Label();
        Label labelEnd = new Label();

        // if value != 0 (true), jump to labelTrue
        mv.visitJumpInsn(IFNE, labelTrue);

        // if false (0), push 1 (true)
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, labelEnd);

        // if true (non-zero), push 0 (false)
        mv.visitLabel(labelTrue);
        mv.visitInsn(ICONST_0);

        mv.visitLabel(labelEnd);

    }

    private void generateCodeForUnaryOperation(MethodVisitor mv, UnaryOperationNode node) {
        // Get the type of expression
        TypeNode expType = findTypeOfExpression(node.node);

        // Generate expression
        codeGenerationExpression(mv, node.node);

        switch (node.operator) {
            case "-":
                generateCodeForUnaryMinus(mv, node, expType);
                break;
            case "!":
                generateCodeForNon(mv, node);
                break;
        }
    }

    ///

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

    /// Generate code for primary expression

    private void generateCodeForLiteral(MethodVisitor mv, LiteralNode node) {
        // Integer
        if (typeEquals(node.literal, IntegerNumber.class)) {
            mv.visitLdcInsn(Integer.parseInt(node.literal.value));
        }
        // Float
        else if (typeEquals(node.literal, FloatNumber.class)) {
            mv.visitLdcInsn(Float.parseFloat(node.literal.value));
        }
        // String
        else if (typeEquals(node.literal, StringValue.class)) {
            mv.visitLdcInsn(node.literal.value);
        }
        // False
        else if (typeEquals(node.literal, FalseKeyword.class)) {
            mv.visitInsn(ICONST_0);
        }
        // True
        else if (typeEquals(node.literal, TrueKeyword.class)) {
            mv.visitInsn(ICONST_1);
        }
    }

    private void generateCodeForArrayLiteral(MethodVisitor mv, ArrayLiteralNode node) {

        codeGenerationExpression(mv, ((ExpressionStatementNode)node.capacity).expression);
        if (!typeToArrayType.containsKey(node.type.type.value)) {
            if (Objects.equals(node.type.type.value, "string")) {
                mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
            }
            else {
                mv.visitTypeInsn(ANEWARRAY, node.type.type.value);
            }
        }
        else {
            mv.visitIntInsn(NEWARRAY, typeToArrayType.get(node.type.type.value));
        }
    }

    private String getFunctionDescriptor(FunctionCallNode node, FunctionDeclarationNode funDecl, boolean isDecl) {
        StringBuilder descriptor = new StringBuilder();
        descriptor.append("(");
        if (isDecl || !flexibleFunctions.contains(funDecl.functionName)) {
            for (ParameterNode param : funDecl.parameters) {
                TypeNode paramType = (TypeNode) param.type;
                descriptor.append(getTypeOfVariableInBytecode(paramType));
            }
        }
        else {
            for (ASTNode param : node.arguments) {
                TypeNode paramType = findTypeOfExpression(param);
                descriptor.append(getTypeOfVariableInBytecode(paramType));
            }
        }
        descriptor.append(")");
        if (funDecl.returnType != null) {
            descriptor.append(getTypeOfVariableInBytecode((TypeNode)funDecl.returnType));
        }
        else {
            descriptor.append("V");
        }
        return descriptor.toString();
    }

    private void codeFunctionCall(MethodVisitor mv, FunctionCallNode functionCall, FunctionDeclarationNode functionDecl) {
        int i = 0;
        for (ASTNode param : functionCall.arguments) {
            ExpressionStatementNode exprParam = (ExpressionStatementNode) param;
            codeGenerationExpression(mv, exprParam);
            convertIntToFloat(mv, (TypeNode) functionDecl.parameters.get(i).type, findTypeOfExpression(exprParam));
            i++;
        }
        mv.visitMethodInsn(INVOKESTATIC, mainClassName, functionDecl.functionName, getFunctionDescriptor(functionCall, functionDecl, false), false);
    }

    private void codeConstructorCall(MethodVisitor mv, FunctionCallNode functionCall, RecordDeclarationNode recDecl) {
        mv.visitTypeInsn(NEW, recDecl.recordName);       // Allocate object
        mv.visitInsn(DUP);                      // Duplicate reference for constructor
        int i = 0;
        for (ASTNode param : functionCall.arguments) {
            ExpressionStatementNode exprParam = (ExpressionStatementNode) param;
            codeGenerationExpression(mv, exprParam);
            convertIntToFloat(mv, recDecl.fields.get(i).typeNode, findTypeOfExpression(exprParam));
            i++;
        }
        StringBuilder descriptor = new StringBuilder();
        descriptor.append("(");
        for (VariableDeclarationNode param : recDecl.fields) {
            TypeNode paramType = param.typeNode;
            descriptor.append(getTypeOfVariableInBytecode(paramType));
        }
        descriptor.append(")");
        descriptor.append("V");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, recDecl.recordName,"<init>", descriptor.toString(),false);
    }

    private Object generateCodeForComplexVariable(MethodVisitor mv, ASTNode node) {
        // Base case
        if (typeEquals(node, ExpressionStatementNode.class)) {
            codeGenerationExpression(mv, ((ExpressionStatementNode) node).expression);
            return findTypeOfExpression(((ExpressionStatementNode) node).expression);
        }
        if (typeEquals(node, IdentifierNode.class)) {
            IdentifierNode identifierNode = (IdentifierNode) node;
            // Can be VariableDeclaration, RecordDeclaration, FunctionDeclaration
            VariableMeaning varMeaning = getMeaningOfIdentifier(identifierNode.name);
            // If the variable is a simple declared variable, load it, else keep it
            if (typeEquals(varMeaning.meaning.node, VariableDeclarationNode.class)) {
                VariableDeclarationNode varNode = (VariableDeclarationNode) varMeaning.meaning.node;
                if (varMeaning.isGlobal) {
                    mv.visitFieldInsn(GETSTATIC, mainClassName, identifierNode.name, getTypeOfVariableInBytecode(varNode.typeNode));
                }
                else {
                    mv.visitVarInsn(getTypeToLoadOpcode(varNode.typeNode), varMeaning.meaning.index);
                }
            }
            return varMeaning;
        }

        // Function call (Can also be Record Constructor)
        if (typeEquals(node, FunctionCallNode.class)) {
            FunctionCallNode functionCallNode = (FunctionCallNode) node;
            Object meaningFunctionCallNode = ((VariableMeaning) generateCodeForComplexVariable(mv, functionCallNode.functionNode)).meaning.node;
            if (typeEquals(meaningFunctionCallNode, FunctionDeclarationNode.class)) {
                FunctionDeclarationNode functionDeclarationNode = (FunctionDeclarationNode) meaningFunctionCallNode;

                codeFunctionCall(mv, functionCallNode, functionDeclarationNode);

                // Return the return type of the function
                return functionDeclarationNode.returnType;
            }
            else if (typeEquals(meaningFunctionCallNode, RecordDeclarationNode.class)) {

                RecordDeclarationNode recordDeclarationNode = (RecordDeclarationNode) meaningFunctionCallNode;

                codeConstructorCall(mv, functionCallNode, recordDeclarationNode);

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
            Object meaningArrayNode = generateCodeForComplexVariable(mv, arrayAccessNode.arrayNode);
            if (typeEquals(meaningArrayNode, VariableMeaning.class)) {
                meaningArrayNode = ((VariableMeaning) meaningArrayNode).meaning.node;
            }
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

                codeGenerationExpression(mv, arrayAccessNode.index);
                mv.visitInsn(getArrayTypeToLoadOpcode(nodeType));

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
            Object meaningRecordNode = generateCodeForComplexVariable(mv, recordAccessNode.recordNode);
            if (typeEquals(meaningRecordNode, VariableMeaning.class)) {
                meaningRecordNode = ((VariableMeaning) meaningRecordNode).meaning.node;
            }
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

                mv.visitFieldInsn(GETFIELD, nodeType.type.value, recordAccessNode.name, getTypeOfVariableInBytecode(records.get(nodeType.type.value).get(recordAccessNode.name).typeNode));

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

    private void generateCodeForPrimaryExpression(MethodVisitor mv, ASTNode node) {
        // If literal
        if (typeEquals(node, LiteralNode.class)) {
            generateCodeForLiteral(mv, (LiteralNode) node);
        }
        // If Array Literal
        else if (typeEquals(node, ArrayLiteralNode.class)) {
            generateCodeForArrayLiteral(mv, (ArrayLiteralNode) node);
        }
        // Complex variable (simple variable, field access, array access or function call)
        else {
            generateCodeForComplexVariable(mv, node);
        }
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
            Object meaningFunctionCallNode = ((VariableMeaning) findTypeOfVariable(functionCallNode.functionNode)).meaning.node;
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
            if (typeEquals(meaningArrayNode, VariableMeaning.class)) {
                meaningArrayNode = ((VariableMeaning) meaningArrayNode).meaning.node;
            }
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
            if (typeEquals(meaningRecordNode, VariableMeaning.class)) {
                meaningRecordNode = ((VariableMeaning) meaningRecordNode).meaning.node;
            }
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
        if (typeEquals(meaning, VariableMeaning.class)) {
            VariableMeaning varMean = (VariableMeaning) meaning;
            VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) varMean.meaning.node;
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

    /// Generate code for expression

    private void codeGenerationExpression(MethodVisitor mv, ASTNode node) {
        if (typeEquals(node, ExpressionStatementNode.class)) {
            codeGenerationExpression(mv, ((ExpressionStatementNode) node).expression);
        }
        // Binary operator
        else if (typeEquals(node, BinaryOperationNode.class)) {
            generateCodeForBinaryOperation(mv, (BinaryOperationNode) node);
        }
        // Unary operator
        else if (typeEquals(node, UnaryOperationNode.class)) {
            generateCodeForUnaryOperation(mv, (UnaryOperationNode) node);
        }
        // Primary expression
        else {
            generateCodeForPrimaryExpression(mv, node);
        }
    }

    ///

    public TypeNode findTypeOfExpression(ASTNode node) {
        if (typeEquals(node, ExpressionStatementNode.class)) {
            return findTypeOfExpression(((ExpressionStatementNode) node).expression);
        }
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

    /// Parse the input of the target path and file

    private void getPathNames() {
        // Create the name of the main class and
        // get the output folder and file names
        // Default main class name
        outputFileName = "Main.class";
        // Default path name
        outputFolderName = "ClassFiles/";
        // If the path is specified
        if (outputFolderAndFileName != null) {
            StringBuilder sbOutputFolderName = new StringBuilder(outputFolderAndFileName);
            StringBuilder sbOutputFileName = new StringBuilder();
            int pathLength = sbOutputFolderName.length();
            char lastPathChar = sbOutputFolderName.charAt(pathLength - 1);
            while (lastPathChar != '/') {
                sbOutputFileName.append(lastPathChar);
                sbOutputFolderName.deleteCharAt(pathLength - 1);
                pathLength--;
                lastPathChar = sbOutputFolderName.charAt(pathLength - 1);
            }
            // If the file name was also specified
            if (!sbOutputFileName.isEmpty()) {
                sbOutputFileName.reverse();
                // update the output file name
                outputFileName = sbOutputFileName.toString();
            }
            // output the output folder name
            outputFolderName = sbOutputFolderName.toString();
        }
        // Create the main class name
        StringBuilder sbMainClassName = new StringBuilder(outputFileName);

        //delete the ".class" chars to only keep the name
        for (int i = 0; i < 6; i++) {
            sbMainClassName.deleteCharAt(sbMainClassName.length()-1);
        }
        // make it to string
        mainClassName = sbMainClassName.toString();

        // Define the directory path
        File directory = new File(outputFolderName);

        // Attempt to create the directory
        directory.mkdirs();
    }

    /// Code for the main entry point function and all built-in functions

    private void codeMainEntryPoint(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        mv.visitCode();

        mv.visitMethodInsn(INVOKESTATIC, mainClassName, "main", "()V", false);

        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void codeWriteFunction(ClassWriter cw, TypeNode inputType) {
        int loadOp = getTypeToLoadOpcode(inputType);
        String stringType = getTypeOfVariableInBytecode(inputType);

        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "write",
                "(" + stringType + ")V",
                null,
                null
        );

        mv.visitCode();

        // Get System.out
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        // Load the string argument (index 0 since it's static method)
        mv.visitVarInsn(loadOp, 0);

        // Call PrintStream.print(String)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(" + stringType + ")V", false);

        // Return
        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
    }

    private void codeWritelnFunction(ClassWriter cw, TypeNode inputType) {
        int loadOp = getTypeToLoadOpcode(inputType);
        String stringType = getTypeOfVariableInBytecode(inputType);

        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "writeln",
                "(" + stringType + ")V",
                null,
                null
        );

        mv.visitCode();

        // Get System.out
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        // Load the string argument (index 0 since it's static method)
        mv.visitVarInsn(loadOp, 0);

        // Call PrintStream.print(String)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(" + stringType + ")V", false);

        // Return
        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
    }

    private void codeWriteIntFunction(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "writeInt",
                "(I)V",
                null,
                null
        );

        mv.visitCode();

        // Get System.out
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        // Load the int argument (index 0)
        mv.visitVarInsn(ILOAD, 0);

        // Call PrintStream.print(int)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(I)V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // Automatically computed
        mv.visitEnd();

        cw.visitEnd();
    }

    private void codeWriteFloatFunction(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "writeFloat",
                "(F)V",
                null,
                null
        );

        mv.visitCode();

        // Get System.out
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        // Load the int argument (index 0)
        mv.visitVarInsn(FLOAD, 0);

        // Call PrintStream.print(int)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(F)V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // Automatically computed
        mv.visitEnd();

        cw.visitEnd();
    }

    private void codeLenFunction(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "len",
                "(Ljava/lang/String;)I", // (String) -> int
                null,
                null
        );

        mv.visitCode();

        // Load the string parameter (index 0)
        mv.visitVarInsn(ALOAD, 0);

        // Call String.length() -> int
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);

        // Return the int result
        mv.visitInsn(IRETURN);

        mv.visitMaxs(0, 0); // Let ASM calculate stack/local sizes
        mv.visitEnd();

        cw.visitEnd();
    }

    private void codeChrFunction(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "chr",
                "(I)Ljava/lang/String;",
                null,
                null
        );

        mv.visitCode();

        // Load the int parameter (index 0)
        mv.visitVarInsn(ILOAD, 0);

        // Call Integer.toString(int)
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false);

        // Return the resulting string
        mv.visitInsn(ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
    }

    private void codeFloorFunction(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "floor",
                "(F)I", // (float) -> int
                null,
                null
        );

        mv.visitCode();

        // Load the float parameter
        mv.visitVarInsn(FLOAD, 0);

        // Convert float to double (Math.floor expects double)
        mv.visitInsn(F2D);

        // Call Math.floor(double)
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "floor", "(D)D", false);

        // Convert result back to float
        mv.visitInsn(D2I);

        // Return float
        mv.visitInsn(IRETURN);

        mv.visitMaxs(0, 0); // Let ASM compute the stack size
        mv.visitEnd();

        cw.visitEnd();
    }

    private void codeReadStringFunction(ClassWriter cw) {
        // public static String readString()
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "readString",
                "()Ljava/lang/String;",
                null,
                null
        );

        mv.visitCode();

        // Create new Scanner
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

        // Call nextLine on Scanner
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false);

        // Return the result
        mv.visitInsn(ARETURN);

        mv.visitMaxs(0, 0); // Let ASM calculate
        mv.visitEnd();
    }

    public void codeReadFloatFunction(ClassWriter cw) {
        // public static String readFloat()
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "readFloat",
                "()F",
                null,
                null
        );

        mv.visitCode();

        // Create new Scanner
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

        // Call nextLine on Scanner
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextFloat", "()F", false);

        // Return the result
        mv.visitInsn(FRETURN);

        mv.visitMaxs(0, 0); // Let ASM calculate
        mv.visitEnd();
    }

    public void codeReadIntFunction(ClassWriter cw) {
        // public static String readFloat()
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                "readInt",
                "()I",
                null,
                null
        );

        mv.visitCode();

        // Create new Scanner
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

        // Call nextLine on Scanner
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);

        // Return the result
        mv.visitInsn(IRETURN);

        mv.visitMaxs(0, 0); // Let ASM calculate
        mv.visitEnd();
    }

    private void codeForMainAndBuiltIn(ClassWriter cw) {
        codeMainEntryPoint(cw);
        codeWriteFunction(cw, new TypeNode(new StringType(), false));
        codeWriteFunction(cw, new TypeNode(new IntType(), false));
        codeWriteFunction(cw, new TypeNode(new FloatType(), false));
        codeWriteFunction(cw, new TypeNode(new BoolType(), false));
        codeWritelnFunction(cw, new TypeNode(new StringType(), false));
        codeWritelnFunction(cw, new TypeNode(new IntType(), false));
        codeWritelnFunction(cw, new TypeNode(new FloatType(), false));
        codeWritelnFunction(cw, new TypeNode(new BoolType(), false));
        codeWriteIntFunction(cw);
        codeWriteFloatFunction(cw);
        codeLenFunction(cw);
        codeChrFunction(cw);
        codeFloorFunction(cw);
        codeReadStringFunction(cw);
        codeReadFloatFunction(cw);
        codeReadIntFunction(cw);
    }

    /// Main function (entry point)
    public void codeGeneration() throws IOException {
        // Get the paths of the target files, the name of the main file and the name of the main class
        getPathNames();
        // Create the main class
        ClassWriter mainCW = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        mainCW.visit(V17, ACC_PUBLIC, mainClassName, null, "java/lang/Object", null);
        // Create initial global scope
        pushScope();
        // Add to global scope all function declarations, including built-in ones
        addAllFunctionDeclarationsToScope(root);
        // Now we add the built-in function in the "built_in_functions.txt" file
        addBuiltInFunctions("src/main/java/compiler/Semantic/built_in_functions.txt");
        // Traverse the AST
        generateGlobalCode(mainCW);
        // Generate the code for the main class initialization
        mainClassInitialization(mainCW);
        // Generate the code for the psvm main entry point function and all built-in functions
        codeForMainAndBuiltIn(mainCW);
        // End the main class visit
        mainCW.visitEnd();

        // Get the main class code
        byte[] mainClassBytecode = mainCW.toByteArray();

        // Write the bytecode to the specific class file
        try (FileOutputStream fos = new FileOutputStream(outputFolderName + outputFileName)) {
            fos.write(mainClassBytecode);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// Code for main class initialization

    private void mainClassInitialization(ClassWriter mainCW) {
        MethodVisitor staticInit = mainCW.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        staticInit.visitCode();
        for (VariableDeclarationNode varDec : initialisedGlobalVariables) {
            codeGenerationExpression(staticInit, ((ExpressionStatementNode)varDec.initializer).expression);
            staticInit.visitFieldInsn(PUTSTATIC, mainClassName, varDec.varName, getTypeOfVariableInBytecode(varDec.typeNode));
        }

        staticInit.visitInsn(RETURN);
        staticInit.visitMaxs(0, 0);
        staticInit.visitEnd();
    }

    /// Code generation for record declaration

    public void checkRecordDeclaration(RecordDeclarationNode node) {
        // Check record name is unique
        testRecordError(node.recordName);
        // Add the record name to the scope so other variables can't have its name
        addVariableToScope(node.recordName, node);
        // Add the record to the `records` map
        addRecord(node);

        // NEW code

        // Create the class (record)
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(javaV, ACC_PUBLIC, node.recordName, null, "java/lang/Object", null);

        // Constructor parameters and return value string
        StringBuilder ctorDescriptor = new StringBuilder("(");
        // For each field, generate code for variable declaration
        for (VariableDeclarationNode field : node.fields) {
            // not static
            checkGlobalVariableDeclaration(cw, field, true, false);
            // append to constructor descriptor the type
            ctorDescriptor.append(getTypeOfVariableInBytecode(field.typeNode));
        }
        // close the parenthesis and put V (void)
        ctorDescriptor.append(")V");

        // Now we need to create the constructor
        MethodVisitor ctor = cw.visitMethod(ACC_PUBLIC, "<init>", ctorDescriptor.toString(), null, null);
        ctor.visitCode();

        // call super()
        ctor.visitVarInsn(ALOAD, 0);
        ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        // initialize variable counter to 1
        int currentVarCounter = 1;

        // for each field
        for (VariableDeclarationNode field : node.fields) {
            // load `THIS`
            ctor.visitVarInsn(ALOAD, 0);
            // load the value of the variable from the constructor
            ctor.visitVarInsn(getTypeToLoadOpcode(field.typeNode), currentVarCounter);
            // put the value in the field
            ctor.visitFieldInsn(PUTFIELD, node.recordName, field.varName, getTypeOfVariableInBytecode(field.typeNode));
            // increase the variable counter
            currentVarCounter++;
        }

        // return the constructor and verify stack size + local variable count
        ctor.visitInsn(RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();

        cw.visitEnd();

        byte[] recordBytecode = cw.toByteArray();

        try (FileOutputStream fos = new FileOutputStream(outputFolderName + node.recordName + ".class")) {
            fos.write(recordBytecode);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /// Convert int value on top of the stack to float if the required is float

    private void convertIntToFloat(MethodVisitor mv, TypeNode expectedType, TypeNode actualType) {
        if (Objects.equals(expectedType.type.value, "float") &&
                Objects.equals(actualType.type.value, "int")) {
            mv.visitInsn(I2F);
            actualType.type.value = "float";
        }
    }

    // This function is very tricky:
    // You need to generate the code vor the complex variable besides the "last" access (array access, field or just variable)
    // Then push on the stack the value and then store the value
    // Here `node` is the left complex variable
    private Object generateCodeForAssignment(MethodVisitor mv, ASTNode node, boolean isLast, ASTNode rightExpr) {
        // Base case
        if (typeEquals(node, ExpressionStatementNode.class)) {
            codeGenerationExpression(mv, ((ExpressionStatementNode) node).expression);
            return findTypeOfExpression(((ExpressionStatementNode) node).expression);
        }
        if (typeEquals(node, IdentifierNode.class)) {
            IdentifierNode identifierNode = (IdentifierNode) node;
            // Can be VariableDeclaration, RecordDeclaration, FunctionDeclaration
            VariableMeaning varMeaning = getMeaningOfIdentifier(identifierNode.name);
            // The variable is always a variable declaration node in an assignment
            VariableDeclarationNode varNode = (VariableDeclarationNode) varMeaning.meaning.node;
            if (!isLast) {
                if (varMeaning.isGlobal) {
                    mv.visitFieldInsn(GETSTATIC, mainClassName, identifierNode.name, getTypeOfVariableInBytecode(varNode.typeNode));
                } else {
                    mv.visitVarInsn(getTypeToLoadOpcode(varNode.typeNode), varMeaning.meaning.index);
                }
            }
            else {
                // load the assigned value
                codeGenerationExpression(mv, rightExpr);
                if (varMeaning.isGlobal) {
                    mv.visitFieldInsn(PUTSTATIC, mainClassName, varNode.varName, getTypeOfVariableInBytecode(varNode.typeNode));
                }
                else {
                    convertIntToFloat(mv, varNode.typeNode, findTypeOfExpression(rightExpr));
                    mv.visitVarInsn(getTypeToStoreOpcode(varNode.typeNode), varMeaning.meaning.index);
                }
            }
            return varMeaning;
        }

        // Array access
        if (typeEquals(node, ArrayAccessNode.class)) {
            ArrayAccessNode arrayAccessNode = (ArrayAccessNode) node;
            // Recursively find the meaning of the node array
            Object meaningArrayNode = generateCodeForAssignment(mv, arrayAccessNode.arrayNode, false, rightExpr);
            if (typeEquals(meaningArrayNode, VariableMeaning.class)) {
                meaningArrayNode = ((VariableMeaning) meaningArrayNode).meaning.node;
            }
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

                // always push the array size
                codeGenerationExpression(mv, arrayAccessNode.index);

                if (!isLast) {
                    // if the array access is not last, just load it
                    mv.visitInsn(getArrayTypeToLoadOpcode(nodeType));
                }
                else {
                    // store the value in the array
                    // load the assigned value
                    codeGenerationExpression(mv, rightExpr);

                    TypeNode modifType = findTypeOfExpression(rightExpr);
                    convertIntToFloat(mv, nodeType, modifType);
                    mv.visitInsn(getArrayTypeToStoreOpcode(modifType));
                }

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
            Object meaningRecordNode = generateCodeForAssignment(mv, recordAccessNode.recordNode, false, rightExpr);
            if (typeEquals(meaningRecordNode, VariableMeaning.class)) {
                meaningRecordNode = ((VariableMeaning) meaningRecordNode).meaning.node;
            }
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

                if (!isLast) {
                    mv.visitFieldInsn(GETFIELD, nodeType.type.value, recordAccessNode.name, getTypeOfVariableInBytecode(records.get(nodeType.type.value).get(recordAccessNode.name).typeNode));
                }
                else {
                    codeGenerationExpression(mv, rightExpr);
                    TypeNode modifType = findTypeOfExpression(rightExpr);
                    convertIntToFloat(mv, records.get(nodeType.type.value).get(recordAccessNode.name).typeNode, modifType);
                    mv.visitFieldInsn(PUTFIELD, nodeType.type.value, recordAccessNode.name, getTypeOfVariableInBytecode(modifType));
                }
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

    ///Check function declaration and block node

    public void checkBlockNode(MethodVisitor mv, BlockNode node) {
        pushScope();
        for (ASTNode statement : node.statements) {
            // Variable declaration
            if (typeEquals(statement, VariableDeclarationNode.class)) {
                // NEW
                checkVariableDeclaration(mv, (VariableDeclarationNode) statement);
            }
            // Assignment
            else if (typeEquals(statement, AssignmentNode.class)) {
                AssignmentNode assignmentNode = (AssignmentNode) statement;
                TypeNode leftType = findTypeOfPrimaryExpression(assignmentNode.left);
                expectExpressionToHaveType((ExpressionStatementNode) assignmentNode.right, leftType);
                // NEW
                generateCodeForAssignment(mv, assignmentNode.left, true, assignmentNode.right);
            }
            // Block
            else if (typeEquals(statement, BlockNode.class)) {
                checkBlockNode(mv, (BlockNode) statement);
            }
            // If
            else if (typeEquals(statement, IfStatementNode.class)) {
                IfStatementNode ifStatementNode = (IfStatementNode) statement;

                Label endLabel = new Label();
                Label currentLabel = new Label();

                codeGenerationExpression(mv, ifStatementNode.condition);
                mv.visitJumpInsn(IFEQ, currentLabel);

                checkBlockNode(mv, (BlockNode) ifStatementNode.ifBlock);

                mv.visitJumpInsn(GOTO, endLabel);

                for (ElseIfBranchNode elseIfBranchNode : ifStatementNode.elseIfBranches) {
                    mv.visitLabel(currentLabel);
                    currentLabel = new Label();

                    codeGenerationExpression(mv, elseIfBranchNode.condition);
                    mv.visitJumpInsn(IFEQ, currentLabel);

                    checkBlockNode(mv, (BlockNode) elseIfBranchNode.block);
                    mv.visitJumpInsn(GOTO, endLabel);
                }

                mv.visitLabel(currentLabel);

                if (ifStatementNode.elseBlock != null) {
                    checkBlockNode(mv, (BlockNode) ifStatementNode.elseBlock);
                    mv.visitJumpInsn(GOTO, endLabel);
                }
                mv.visitLabel(endLabel);
            }
            // While
            else if (typeEquals(statement, WhileStatementNode.class)) {
                WhileStatementNode whileStatementNode = (WhileStatementNode) statement;

                Label loopLabel = new Label();
                Label endLabel = new Label();

                mv.visitLabel(loopLabel);
                codeGenerationExpression(mv, whileStatementNode.condition);
                mv.visitJumpInsn(IFEQ, endLabel);

                checkBlockNode(mv, (BlockNode) whileStatementNode.body);

                mv.visitJumpInsn(GOTO, loopLabel);
                mv.visitLabel(endLabel);
            }
            // For
            else if (typeEquals(statement, ForStatementNode.class)) {
                ForStatementNode forStatementNode = (ForStatementNode) statement;
                expectExpressionToHaveType(new ExpressionStatementNode(forStatementNode.variable), new TypeNode(new FloatType(), false));
                expectExpressionToHaveType((ExpressionStatementNode) forStatementNode.initialValue, new TypeNode(new FloatType(), false));
                expectExpressionToHaveType((ExpressionStatementNode) forStatementNode.maxValue, new TypeNode(new FloatType(), false));
                expectExpressionToHaveType((ExpressionStatementNode) forStatementNode.incrementValue, new TypeNode(new FloatType(), false));

                //TypeNode typeVar = findTypeOfExpression(forStatementNode.variable);

                // assign initial value to the variable
                generateCodeForAssignment(mv, forStatementNode.variable, true, forStatementNode.initialValue);

                Label loopLabel = new Label();
                Label endLabel = new Label();

                mv.visitLabel(loopLabel);
                codeGenerationExpression(mv, new BinaryOperationNode("<=", forStatementNode.variable, forStatementNode.maxValue));
                mv.visitJumpInsn(IFEQ, endLabel);

                checkBlockNode(mv, (BlockNode) forStatementNode.body);

                generateCodeForAssignment(mv, forStatementNode.variable, true, new BinaryOperationNode("+", forStatementNode.variable, forStatementNode.incrementValue));
                mv.visitJumpInsn(GOTO, loopLabel);

                mv.visitLabel(endLabel);
            }
            // Function call
            else if (typeEquals(statement, FunctionCallNode.class)) {
                FunctionCallNode functionCallNode = (FunctionCallNode) statement;
                findTypeOfVariable(functionCallNode);

                // NEW
                codeGenerationExpression(mv, functionCallNode);
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
                // NEW
                // Push the value on the stack
                codeGenerationExpression(mv, returnStatementNode.value);

                //
                mv.visitInsn(getTypeForReturnBytecode(returnedType));

                // Stack verify
                mv.visitMaxs(0, 0);
                mv.visitEnd();

            }
            else {
                throw new RuntimeException("Statement unrecognized");
            }
        }
        popScope();
    }

    public void checkFunctionDeclaration(ClassWriter cw, FunctionDeclarationNode node) {
        // Add a scope for the function
        pushScope();
        // Set the returned type to null
        currentFunctionReturnedType = null;

        // Write code for function declaration
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, node.functionName, getFunctionDescriptor(null, node, true), null, null);
        mv.visitCode();

        // Add all parameters to scope
        for (ParameterNode parameter : node.parameters) {
            // Make the Param a Variable Declaration so it's easier to manage the scope objects
            addVariableToScope(parameter.paramName, new VariableDeclarationNode(false, parameter.paramName, (TypeNode) parameter.type, null));
        }
        BlockNode functionBlock = (BlockNode) node.functionBody;
        checkBlockNode(mv, functionBlock);
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
        if (node.returnType == null) {
            mv.visitInsn(RETURN);

            // Stack verify
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Pop the scope when exiting function
        popScope();
    }

    /// Code generation for global variables (they can be global variables or fields in records)

    // Need separate functions for global and local variables because global have class writer as parameter
    // and local have writeCode
    public void checkGlobalVariableDeclaration(ClassWriter cw, VariableDeclarationNode node, boolean isRecordField, boolean isStatic) {
        // Add the variable name to the scope
        if (!isRecordField) {
            addVariableToScope(node.varName, node);
        }

        // FINAL flag
        int optACC_FINAL = 0;
        if (node.isFinal) {
            optACC_FINAL = ACC_FINAL;
        }

        // STATIC flag
        int optACC_STATIC = 0;
        if (isStatic) {
            optACC_STATIC = ACC_STATIC;
        }

        cw.visitField(ACC_PUBLIC + optACC_STATIC + optACC_FINAL, node.varName, getTypeOfVariableInBytecode(node.typeNode), null, null).visitEnd();

        if (node.initializer != null) {
            initialisedGlobalVariables.add(node);
        }
    }

    public void checkVariableDeclaration(MethodVisitor mv, VariableDeclarationNode node) {
        // Add the variable name to the scope
        addVariableToScope(node.varName, node);

        if (node.initializer != null) {
            codeGenerationExpression(mv, node.initializer);
            if (Objects.equals(findTypeOfExpression(node.initializer).type.value, "int") &&
                    Objects.equals(node.typeNode.type.value, "float")) {
                mv.visitInsn(I2F);
            }
            mv.visitVarInsn(getTypeToStoreOpcode(node.typeNode), getMeaningOfIdentifier(node.varName).meaning.index);
        }
    }

    /// Generate global code (var declarations, records and functions)

    public void generateGlobalCode(ClassWriter mainCW) {
        for (ASTNode node : root.nodes) {
            if (typeEquals(node, VariableDeclarationNode.class)) {
                checkGlobalVariableDeclaration(mainCW, (VariableDeclarationNode) node, false, true);
            }
            else if (typeEquals(node, FunctionDeclarationNode.class)) {
                checkFunctionDeclaration(mainCW, (FunctionDeclarationNode) node);
            }
            else if (typeEquals(node, RecordDeclarationNode.class)) {
                checkRecordDeclaration((RecordDeclarationNode) node);
            }
        }
    }

    public static void main(String[] args) throws IOException {
    }
}
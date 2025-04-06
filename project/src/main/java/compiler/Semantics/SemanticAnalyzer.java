package compiler.Semantics;

import compiler.Lexer.Symbol;
import compiler.Parser.CFG.*;

import java.util.*;

public class SemanticAnalyzer {
    private final Map<String, FunctionDeclarationNode> declaredFunctions = new HashMap<>();
    private final SymbolTable variables = new SymbolTable();
    private final Map<String, Map<String, TypeNode>> recordDefinitions = new HashMap<>();

    public void analyze(ASTNode root) {
        if (!(root instanceof ProgramNode)) return;
        ProgramNode program = (ProgramNode) root;

        for (ASTNode node : program.getProgramNodes()) {
            if (node instanceof RecordDeclarationNode) {
                RecordDeclarationNode record = (RecordDeclarationNode) node;
                String recordName = record.getRecordName();
                Map<String, TypeNode> fields = new HashMap<>();
                for (VariableDeclarationNode field : record.getRecordFields()) {
                    fields.put(field.getVarName(), field.getVarType());
                }
                recordDefinitions.put(recordName, fields);
            } else if (node instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode func = (FunctionDeclarationNode) node;
                declaredFunctions.put(func.getFunctionName(), func);
            }
        }

        for (ASTNode node : program.getProgramNodes()) {
            if (node instanceof FunctionDeclarationNode) {
                analyzeFunction((FunctionDeclarationNode) node);
            }
        }
    }

    private void analyzeFunction(FunctionDeclarationNode functionNode) {
        variables.enterScope();

        for (ParameterNode param : functionNode.getParameters()) {
            variables.declare(param.getParamName(), param.getParamType());
        }

        getAllVariables(functionNode.getFunctionBody());
        analyzeBlock(functionNode.getFunctionBody());

        checkReturnTypes(functionNode);

        variables.exitScope();
    }

    private void checkReturnTypes(FunctionDeclarationNode functionNode) {
        String expectedReturnType = ((TypeNode) functionNode.getReturnType()).getTypeNode().type;
        List<ReturnStatementNode> returns = collectReturnStatements(functionNode.getFunctionBody());

        for (ReturnStatementNode ret : returns) {
            Symbol result = analyzeExpression(ret.getReturnValue());
            expectType(result.type, expectedReturnType);
        }
    }

    private List<ReturnStatementNode> collectReturnStatements(ASTNode node) {
        List<ReturnStatementNode> result = new ArrayList<>();

        if (node instanceof ReturnStatementNode) {
            result.add((ReturnStatementNode) node);
        } else if (node instanceof BlockNode) {
            for (ASTNode stmt : ((BlockNode) node).getStatements()) {
                result.addAll(collectReturnStatements(stmt));
            }
        } else if (node instanceof IfStatementNode) {
            result.addAll(collectReturnStatements(((IfStatementNode) node).getIfBlock()));
            if (((IfStatementNode) node).getElseBlock() != null) {
                result.addAll(collectReturnStatements(((IfStatementNode) node).getElseBlock()));
            }
        } else if (node instanceof WhileStatementNode) {
            result.addAll(collectReturnStatements(((WhileStatementNode) node).getWhileBody()));
        } else if (node instanceof ForStatementNode) {
            result.addAll(collectReturnStatements(((ForStatementNode) node).getBody()));
        }

        return result;
    }

    private void getAllVariables(ASTNode blockNode) {
        if (!(blockNode instanceof BlockNode)) return;
        BlockNode block = (BlockNode) blockNode;

        for (ASTNode stmt : block.getStatements()) {
            if (stmt instanceof VariableDeclarationNode) {
                VariableDeclarationNode varDecl = (VariableDeclarationNode) stmt;
                variables.declare(varDecl.getVarName(), varDecl.getVarType());
            } else if (stmt instanceof ForStatementNode) {
                variables.enterScope();
                getAllVariables(((ForStatementNode) stmt).getBody());
                variables.exitScope();
            }
        }
    }

    private void analyzeBlock(ASTNode blockNode) {
        if (!(blockNode instanceof BlockNode)) return;
        BlockNode block = (BlockNode) blockNode;

        for (ASTNode stmt : block.getStatements()) {
            if (stmt instanceof AssignmentNode) {
                AssignmentNode assign = (AssignmentNode) stmt;
                Symbol right = analyzeExpression(assign.getRight());
                String leftType = resolveVariableType(((IdentifierNode) assign.getLeft()).getIdentifierName());
                expectType(right.type, leftType);
            } else if (stmt instanceof IfStatementNode) {
                IfStatementNode ifStmt = (IfStatementNode) stmt;
                analyzeExpression(ifStmt.getCondition());

                variables.enterScope();
                getAllVariables(ifStmt.getIfBlock());
                analyzeBlock(ifStmt.getIfBlock());
                variables.exitScope();

                if (ifStmt.getElseBlock() != null) {
                    variables.enterScope();
                    getAllVariables(ifStmt.getElseBlock());
                    analyzeBlock(ifStmt.getElseBlock());
                    variables.exitScope();
                }
            } else if (stmt instanceof WhileStatementNode) {
                WhileStatementNode whileStmt = (WhileStatementNode) stmt;
                analyzeExpression(whileStmt.getWhileCondition());

                variables.enterScope();
                getAllVariables(whileStmt.getWhileBody());
                analyzeBlock(whileStmt.getWhileBody());
                variables.exitScope();
            } else if (stmt instanceof ForStatementNode) {
                ForStatementNode forStmt = (ForStatementNode) stmt;
                analyzeExpression(forStmt.getInitialValue());
                analyzeExpression(forStmt.getMaxValue());

                variables.enterScope();
                analyzeBlock(forStmt.getBody());
                variables.exitScope();
            } else if (stmt instanceof FunctionCallNode) {
                checkFunctionCall((FunctionCallNode) stmt);
            }
        }
    }

    private Symbol analyzeExpression(ASTNode expr) {
        ASTNode e = (expr instanceof ExpressionStatementNode)
                ? ((ExpressionStatementNode) expr).getExpression()
                : expr;

        if (e instanceof LiteralNode) {
            String type = ((LiteralNode) e).getLiteralSymbol().type;
            switch (type) {
                case "INTEGER_NUMBER": return new Symbol("INT_TYPE", null);
                case "FLOAT_NUMBER": return new Symbol("FLOAT_TYPE", null);
                case "BOOL_TYPE": return new Symbol("BOOL_TYPE", null);
                case "STRING_TYPE": return new Symbol("STRING_TYPE", null);
            }
        } else if (e instanceof IdentifierNode) {
            String name = ((IdentifierNode) e).getIdentifierName();
            String type = resolveVariableType(name);
            if (type == null) throw new RuntimeException("Undeclared variable: " + name);
            return new Symbol(type, name);
        } else if (e instanceof BinaryOperationNode) {
            BinaryOperationNode binOp = (BinaryOperationNode) e;
            Symbol left = analyzeExpression(binOp.getLeft());
            Symbol right = analyzeExpression(binOp.getRight());
            expectType(left.type, right.type);
            return new Symbol(left.type, null);
        } else if (e instanceof FunctionCallNode) {
            return checkFunctionCall((FunctionCallNode) e);
        } else if (e instanceof RecordAccessNode) {
            return checkRecordAccess((RecordAccessNode) e);
        }

        throw new RuntimeException("Unsupported expression: " + e);
    }

    private Symbol checkRecordAccess(RecordAccessNode recordAccess) {
        Symbol record = analyzeExpression(recordAccess.getRecordNode());

        ASTNode recordNode = variables.resolve(record.value);
        assert recordNode != null;
        String recordType = ((TypeNode) recordNode).getTypeNode().value;

        String fieldName = recordAccess.getRecordName();

        // checking if the record we called exists and if it is a record
        // if it is, we get its fields and check if the filed we accessed exists in the structure

        Map<String, TypeNode> fields = recordDefinitions.get(recordType);
        if (fields == null) {
            throw new RuntimeException("Unknown record type: " + recordType);
        }

        System.out.println("eroarea cred ca vine de la accesarea elementelor din structura in functii, inca nu sunt sigur, dar poti sa testezi in fisierul TestSemantics");

        TypeNode fieldTypeNode = fields.get(fieldName);
        if (fieldTypeNode == null) {
            throw new RuntimeException("Field '" + fieldName + "' not found in record type: " + recordType);
        }

        return new Symbol(fieldTypeNode.getTypeNode().type, null);
    }

    private Symbol checkFunctionCall(FunctionCallNode call) {
        String name = ((IdentifierNode) call.getFunctionNode()).getIdentifierName();
        FunctionDeclarationNode func = declaredFunctions.get(name);

        if (func == null) {
            throw new RuntimeException("Undeclared function: " + name);
        }

        List<ASTNode> actualArgs = call.getArguments();
        List<ParameterNode> formalArgs = func.getParameters();

        if (actualArgs.size() != formalArgs.size()) {
            throw new RuntimeException("Argument count mismatch in call to: " + name);
        }

        for (int i = 0; i < actualArgs.size(); i++) {
            Symbol actual = analyzeExpression(actualArgs.get(i));
            String expected = ((TypeNode) formalArgs.get(i).getParamType()).getTypeNode().type;
            expectType(actual.type, expected);
        }

        return new Symbol(((TypeNode) func.getReturnType()).getTypeNode().type, null);
    }

    private String resolveVariableType(String name) {
        ASTNode type = variables.resolve(name);
        if (type instanceof TypeNode) {
            return ((TypeNode) type).getTypeNode().type;
        }
        return null;
    }

    private void expectType(String found, String expected) {
        if (!found.equals(expected)) {
            throw new RuntimeException("Type mismatch: expected " + expected + " but found " + found);
        }
    }

    private static class SymbolTable {
        private final Deque<Map<String, ASTNode>> scopes = new ArrayDeque<>();

        public void enterScope() {
            scopes.push(new HashMap<>());
        }

        public void exitScope() {
            scopes.pop();
        }

        public void declare(String name, ASTNode type) {
            Objects.requireNonNull(scopes.peek()).put(name, type);
        }

        public ASTNode resolve(String name) {
            for (Map<String, ASTNode> scope : scopes) {
                if (scope.containsKey(name)) {
                    return scope.get(name);
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return scopes.toString();
        }
    }
}
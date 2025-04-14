package compiler.Parser.CFG;

import compiler.Lexer.Symbol;

public class ArrayLiteralNode extends ASTNode {
    public ASTNode capacity;
    public TypeNode type;

    public ArrayLiteralNode(ASTNode capacity, TypeNode type) {
        this.capacity = capacity;
        this.type = type;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("ArrayLiteral\n");
        printString.append(indentString).append("  Capacity\n");
        printString.append(capacity.toStringIndent(indent + 2));
        printString.append(indentString).append("  Type\n");
        printString.append(type.toStringIndent(indent + 2));
        return printString.toString();
    }
}
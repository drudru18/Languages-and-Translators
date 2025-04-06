package compiler.Parser.CFG;

import compiler.Lexer.Symbol;

public class LiteralNode extends ASTNode {
    private final Symbol literal;

    public LiteralNode(Symbol literal) {
        this.literal = literal;
    }

    public Symbol getLiteralSymbol() {
        return literal;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("Literal: ").append(literal.type).append(", ").append(literal.value).append("\n");
        return printString.toString();
    }
}

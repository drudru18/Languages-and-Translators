package compiler.Parser.CFG;

import compiler.Lexer.Symbol;

public class TypeNode extends ASTNode {
    public Symbol type;
    public boolean isArray;

    public TypeNode(Symbol type, boolean isArray) {
        this.type = type;
        this.isArray = isArray;
    }

    @Override
    public String toStringIndent(int indent) {
        StringBuilder printString = new StringBuilder();
        String indentString = ASTPrinter.getIndent(indent);
        printString.append(indentString).append("TypeValue\n");
        printString.append(indentString).append("  Type: ").append(type.value).append("\n");
        printString.append(indentString).append("  IsArray: ").append(isArray).append("\n");
        return printString.toString();
    }
}

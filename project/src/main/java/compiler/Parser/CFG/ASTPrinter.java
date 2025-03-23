package compiler.Parser.CFG;

public class ASTPrinter {
    public static void print(ASTNode node) {
        System.out.println(node.toString());
    }

    public static String getIndent(int indent) {
        StringBuilder indentString = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentString.append("  ");
        }
        return indentString.toString();
    }
}

package compiler.Parser.CFG;

abstract public class ASTNode {
    public String toString() {
        return toStringIndent(0);
    }

    public abstract String toStringIndent(int indent);
}

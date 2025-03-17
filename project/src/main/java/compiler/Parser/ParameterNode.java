package compiler.Parser;

public class ParameterNode extends ASTNode {
    public String type;
    public String name;
    public boolean isArray;

    public ParameterNode(String type, String name, boolean isArray) {
        this.type = type;
        this.name = name;
        this.isArray = isArray;
    }

    @Override
    public String toString() {
        return "";
    }
}
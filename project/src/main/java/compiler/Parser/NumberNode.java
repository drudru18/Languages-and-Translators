package compiler.Parser;

public class NumberNode extends ExpressionNode {
    public String value;
    public String type;

    public NumberNode(String value, String type) {
        this.value = value;
        this.type = type;
        //convert(value, type);
    }

    private void convert(String value, String type) {
        if (type.equals("INTEGER_NUMBER")) {
            int newValue = Integer.parseInt(value);
        } else if (type.equals("FLOAT_NUMBER")) {
            float newValue = Float.parseFloat(value);
        }
    }

    @Override
    public String toString() {
        return "";
    }
}

import compiler.Lexer.Lexer;
import compiler.Parser.CFG.ASTNode;
import compiler.Parser.CFG.ASTPrinter;
import compiler.Parser.CFG.ProgramNode;
import compiler.Parser.Parser;
import compiler.Semantic.Semantic;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class TestSemantic {

    @Test
    public void sameFunctionNameScopeError() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "fun main(a int) float {\n" +
                        "\n" +
                        "}\n" +
                        "\n" +
                        "fun main() string {\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        Semantic s = new Semantic((ProgramNode) ast);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> s.findSemanticErrors());
        assertEquals("ScopeError", exception.getMessage());
        for (String fName : s.scopes.peek().keySet()) {
            System.out.println(fName);
        }
    }

    //@Test
    public void FinalVarCantHaveRecordType() {
        Lexer lexer = new Lexer(new StringReader(
                "final var Point;"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void CheckRecordConstructor() {
        int a = 5;
        int b = 4;
        System.out.println(a/b);
        Lexer lexer = new Lexer(new StringReader(
                "a int = -a+1 && b*5 || c.g && d[2];"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void CheckNotFunction() {
        Lexer lexer = new Lexer(new StringReader(
                "a bool = -5.1;"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void CheckExpressionTypesOfLiterals() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "a int = Point(1, 2);"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        //s.findSemanticErrors();
    }

    @Test
    public void testTypesInComplesExpressions() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "Point rec {\n" +
                        "x float;\n" +
                        "y float;\n" +
                        "}\n" +
                        "\n" +
                        "p float = Point(1, 2).y;"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void testTypesInComplexExpressions2() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "Point rec {\n" +
                        "x float;\n" +
                        "y float;\n" +
                        "}\n" +
                        "\n" +
                        "p float = Point(1, 2).y;"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void testArrayAssignment() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "Point rec {\n" +
                        "x float[];\n" +
                        "y float;\n" +
                        "}\n" +
                        "a Point[] = array [5] of Point;"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void testFull() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "final v int = 55;\n" +
                        "\n" +
                        "Point rec {\n" +
                        "x int;\n" +
                        "y int;\n" +
                        "}\n" +
                        "\n" +
                        "fun f(p1 Point, p2 Point) Point {\n" +
                        "return Point(p1.x + p2.x, p1.y + p2.y);\n" +
                        "}\n" +
                        "\n" +
                        "fun main() {\n" +
                        "p1 Point = Point(1, 2);\n" +
                        "p2 Point = Point(2, 3);\n" +
                        "f(p1, p2);\n" +
                        "chr(v);\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void testScopes() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "i int = 1;\n" +
                        "fun main() {\n" +
                        "i = 2;\n" +
                        "i int = 0;\n" +
                        "i = i + 1;\n" +
                        "{\n" +
                        "i int = 3;\n" +
                        "}\n" +
                        "}\n"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void testIngi() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "$Good luck\n" +
                        "\n" +
                        "final message string = \"Hello\";\n" +
                        "final run bool = true;\n" +
                        "\n" +
                        "Point rec {\n" +
                        "    x int;\n" +
                        "    y int;\n" +
                        "}\n" +
                        "\n" +
                        "a int = 3;\n" +
                        "\n" +
                        "fun square(v int) int {\n" +
                        "    return v*v;\n" +
                        "}\n" +
                        "\n" +
                        "fun main() {\n" +
                        "    value int = readInt();\n" +
                        "    p Point = Point(a, a+value);\n" +
                        "    writeInt(square(value));\n" +
                        "    writeln(\"\");\n" +
                        "    i int;\n" +
                        "    for (i, 1, a, 1) {\n" +
                        "        while (value!=0) {\n" +
                        "            if (run){\n" +
                        "                value = value - 1;\n" +
                        "            } else {\n" +
                        "                write(message);\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "    i = (i+2)*2;\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void testIngi2() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "i int = (1+2);"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    //@Test
    public void testIngi3() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "fun f(a int) int {\n" +
                        "return 1;\n" +
                        "}\n" +
                        "\n" +
                        "fun main() {\n" +
                        "f(1.2);\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void testIngi4() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "fun f(a int) int {return 1;}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void test5() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "fun main() {\n" +
                        "a int = 1;\n" +
                        "{\n" +
                        "a bool = true;\n" +
                        "a = false;\n" +
                        "}\n" +
                        "a = 3;\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void test6() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "final a int = 3;\n" +
                        "fun main() {\n" +
                        "a = 5;\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void test7() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "Point rec {\n" +
                        "a int[];\n" +
                        "}\n" +
                        "\n" +
                        "fun main() {\n" +
                        "aa Point[];\n" +
                        "aa = array [5] of Point;\n" +
                        "aa[0] = Point(array [5] of int);\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

    @Test
    public void t2() throws IOException {
        Lexer lexer = new Lexer(new StringReader(
                "Point rec {\n" +
                        "x int;\n" +
                        "y float;\n" +
                        "}\n" +
                        "\n" +
                        "Point2 rec {\n" +
                        "x string;\n" +
                        "y bool;\n" +
                        "z Point;\n" +
                        "}\n" +
                        "\n" +
                        "fun calc(a int, b int) int {\n" +
                        "$a = 1;\n" +
                        "return 1 + 1;\n" +
                        "}\n" +
                        "\n" +
                        "fun newPoint() Point {\n" +
                        "return Point(1, 2.2);\n" +
                        "}\n" +
                        "\n" +
                        "fun voidFunc() {\n" +
                        "b int = 2;\n" +
                        "}\n" +
                        "\n" +
                        "glob int = 2;\n" +
                        "\n" +
                        "\n" +
                        "fun main() {\n" +
                        "a int = 6;\n" +
                        "b int;\n" +
                        "if (a < 4) {\n" +
                        "b = 0;\n" +
                        "}\n" +
                        "else if (a >= 4 && a <= 7){\n" +
                        "b = 1;\n" +
                        "}\n" +
                        "else {\n" +
                        "b = 2;\n" +
                        "}\n" +
                        "$s1 string = \"Hello, \";\n" +
                        "$s2 string = \"world !\";\n" +
                        "$s3 string = s1 + s2;\n" +
                        "$new1 Point = newPoint();\n" +
                        "$x1 int = newPoint().x;\n" +
                        "$arr Point2[] = array[5] of Point2;\n" +
                        "$p1 Point = Point(2, 3.5);\n" +
                        "$p2 Point2 = Point2(\"hi\", false, p1);\n" +
                        "$arr[1] = p2;\n" +
                        "$x float = arr[1].z.y;\n" +
                        "$arr[1].z = Point(3, 4);\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
    }

}

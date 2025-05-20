import compiler.Lexer.Lexer;
import compiler.Parser.CFG.ASTNode;
import compiler.Parser.CFG.ASTPrinter;
import compiler.Parser.Parser;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/// Note: The tests for this phase print the AST recursively found by the Parser and
///       you can see that they print the expected tree nodes
public class TestParser {

    /// Tests:

    /// Variable declarations and assignments
    @Test
    public void test1() {
        Lexer lexer = new Lexer(new StringReader(
                "final a int = 42;\n" +
                        "var123 float = 3.14;\n" +
                        "isOk bool = true;\n" +
                        "message string = \"This is a simple message\";"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    /// Record definition with its fields (the fields are considered variable declarations
    /// with no instantiation and no final keyword)
    @Test
    public void test2() {
        Lexer lexer = new Lexer(new StringReader(
                "Student rec {\n" +
                        "grades int[];\n" +
                        "followedCurses string[];\n" +
                        "height float;\n" +
                        "isMinor bool;\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    /// Functions, loops and expressions
    /// Note: this code can't work without the preceding one, but
    /// it's not Parser's work to verify this
    @Test
    public void test3() {
        Lexer lexer = new Lexer(new StringReader(
                "fun isGraduating(stud Student) bool {\n" +
                        "avg float = 0;\n" +
                        "i int;\n" +
                        "for (i, 0, len(grades)-1, 1) {\n" +
                        "avg = avg + grades[i];\n" +
                        "}\n" +
                        "isOk bool;\n" +
                        "if (avg >= 5) {\n" +
                        "isOk = true;\n" +
                        "}\n" +
                        "else {\n" +
                        "isOk = false;\n" +
                        "}\n" +
                        "return isOk;\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    /// More complex expressions with all sorts of operations
    /// and showing operations ordering in expressions
    @Test
    public void test4() {
        Lexer lexer = new Lexer(new StringReader(
                "fun main() {\n" +
                        "aBool bool = true;\n" +
                        "val1 int = 10;\n" +
                        "val2 float = 8.99;\n" +
                        "if (val1 > val2 * 2 && aBool || (val1 - val2) % 5 == 1){\n" +
                        "writeln(\"yes\");\n" +
                        "}\n" +
                        "else {\n" +
                        "writeln(\"no\");\n" +
                        "}\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    /// Array access and free, comments and rest of
    /// conditional blocks
    @Test
    public void test5() {
        Lexer lexer = new Lexer(new StringReader(
                "fun main() {\n" +
                        "arr1 int[] = array [5] of int;\n" +
                        "arr1[2] = 8;\n" +
                        "$ comment 1\n" +
                        "prod int = 1;\n" +
                        "while (arr1[2] != 0) {\n" +
                        "arr1[2] = arr1[2] - 1;\n" +
                        "prod = prod * 2;\n" +
                        "$ comment 2\n" +
                        "}\n" +
                        "free arr1;\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    /// Field access and function call
    @Test
    public void test6() {
        Lexer lexer = new Lexer(new StringReader(
                "Student rec {\n" +
                        "age int;\n" +
                        "}\n" +
                        "\n" +
                        "fun foo(a int, b int) int {\n" +
                        "return a + b;\n" +
                        "}\n" +
                        "\n" +
                        "fun main() {\n" +
                        "stud1 Student = Student(20);\n" +
                        "writeln(stud1.age);\n" +
                        "writeln(foo(3, 4));\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }
}

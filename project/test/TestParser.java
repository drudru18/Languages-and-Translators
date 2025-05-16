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

public class TestParser {

    @Test
    public void testFinalVariablesDeclaration() {
        Lexer lexer = new Lexer(new StringReader(
                "final height int = 181;\n" +
                        "final weight float = 78.57;\n" +
                        "final isEmpty bool = true;\n" +
                        "final firstMessage string = \"Hi, how are you ?\";"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void testRecordDeclaration() {
        Lexer lexer = new Lexer(new StringReader(
                "Student rec {\n" +
                        "   height float;\n" +
                        "   age int;\n" +
                        "   grades int[];\n" +
                        "   name string;\n" +
                        "}\n" +
                        "\n" +
                        "School rec {\n" +
                        "   name string;\n" +
                        "   location string;\n" +
                        "   students Student[];\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void testFunctionDeclarationComplexExpressionsArrayAccessFunctionCall() {
        Lexer lexer = new Lexer(new StringReader(
                "Student rec {\n" +
                        "   height float;\n" +
                        "   age int;\n" +
                        "   grades int[];\n" +
                        "   name string;\n" +
                        "}\n" +
                        "\n" +
                        "fun gradesAverage(student Student) float {\n" +
                        "   sum int = 0;\n" +
                        "   iter int;\n" +
                        "   for(iter, 0, len(student.grades), 1) {\n" +
                        "      sum = sum + student.grades[iter];\n" +
                        "   }\n" +
                        "   return sum / len(student.grades);\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void testIfElseWhileGlobalVar() {
        Lexer lexer = new Lexer(new StringReader(
                "global bool = false;\n" +
                        "\n" +
                        "fun main() {\n" +
                        "   if(!global){\n" +
                        "      print(\"Not global\");\n" +
                        "   }\n" +
                        "   else if(global){\n" +
                        "      print(\"Global\");\n" +
                        "   }\n" +
                        "   else {\n" +
                        "      print(\"This case is impossible\");\n" +
                        "   }\n" +
                        "   val int = 5;\n" +
                        "   while(val > 0){\n" +
                        "      print(\"Stille positive\");\n" +
                        "   }\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void FreeKeyword() {
        Lexer lexer = new Lexer(new StringReader(
                "fun main() {\n" +
                        "a int[];\n" +
                        "a = array [5] of int;\n" +
                        "free a;\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void t1() {
        Lexer lexer = new Lexer(new StringReader(
                "fun main() {\n" +
                        "a int = 1;\n" +
                        "a.b.c = 3;\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void t2() {
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
                        "p Point2[] = array [5] of Point2;\n" +
                        "\n" +
                        "arr1_first Point2 = Point2(p[0][0], p[0][1], Point(5, 5.5));"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

}

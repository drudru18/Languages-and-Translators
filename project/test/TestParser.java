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
                        "   arr float[] = array of [3];\n" +
                        "   arr[0] = 0.4;\n" +
                        "   arr[1] = 0.576;\n" +
                        "   arr[2] = .612;\n" +
                        "   free arr;\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }
}

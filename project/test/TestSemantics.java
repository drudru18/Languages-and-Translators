import compiler.Lexer.Lexer;
import compiler.Parser.CFG.ASTNode;
import compiler.Parser.CFG.ASTPrinter;
import compiler.Parser.Parser;
import compiler.Semantics.*;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

public class TestSemantics {
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
                        "fun len(obj Object) int {\n" +
                        "   return 5;\n" +
                        "} \n" +
                        "fun gradesAverage(student Student) float {\n" +
                        "   sum string = \"hello\";\n" +
                        "   iter int;\n" +
                        "   for(iter, 0, len(student.grades), 1) {\n" +
                        "      sum = hello + student.grades[iter];\n" +
                        "   }\n" +
                        "   return sum / len(student.grades);\n" +
                        "} \n" +
                        "fun hello(student Student, number int, arr int[]) int[] {\n" +
                        "   sum int = gradesAverage(student);\n" +
                        "   iter int;\n" +
                        "   for(iter, 0, len(student.grades), 1) {\n" +
                        "      sum = hello + student.grades[iter];\n" +
                        "   }\n" +
                        "   return sum / len(student.grades);\n" +
                        "}"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        //ASTPrinter.print(ast);
        semanticAnalyzer.analyze(ast);
    }

}

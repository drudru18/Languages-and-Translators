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
    public void testRecordDeclarationAndMultipleReturnFunction() {
        // incearca sa schimbi tipul de return al unuia dintre ele, o sa vezi ca da eroare
        // e functia collectReturnStatements care colecteaza toate return-urile din functie
        //      si le verifica pe toate

        Lexer lexer = new Lexer(new StringReader(
                "Student rec {\n" +
                        "   height float;\n" +
                        "   age int;\n" +
                        "   grades int[];\n" +
                        "   name string;\n" +
                        "}\n" +
                        "\n" +
                        "fun len(obj Object) string {\n" +
                        "   if (5 < 10) { \n"+
                        "       return \"c'est faux\";\n"+
                        "   }\n"+
                        "   else {\n"+
                        "       return \"c'est vrai\";\n" +
                        "   }\n"+
                        "} \n"
                        ));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        //ASTPrinter.print(ast);
        semanticAnalyzer.analyze(ast);
    }

    @Test
    public void testFunctionDeclarationComplexExpressionsArrayAccessFunctionCall() {
        // (dupa ce lansezi testul asta) incearca sa bagi functia len care e mai sus,
        //      o sa vezi ce alta eroare iti da :)

        // functioneaza cat de cat dar mai e problema cu verificatul rezultatului,
        //      daca spre exemplu eu îi dau un string si functia vrea un array de
        //      string, tot îl ia :(
        Lexer lexer = new Lexer(new StringReader(
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
                    "}"
        ));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        //ASTPrinter.print(ast);
        semanticAnalyzer.analyze(ast);
    }



}

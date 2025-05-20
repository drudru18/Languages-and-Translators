import static org.junit.Assert.assertNotNull;

import compiler.Lexer.Symbol;
import compiler.Lexer.Symbols.EndOfInputType;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import compiler.Lexer.Lexer;

/// Note: The tests for this phase print the Symbols found by the Lexer and
///       you can see that they print the expected Symbols
public class TestLexer {

    /// Function for printing the Symbols after lexing the input file
    public void printSymbols(String content) {
        Lexer lexer = new Lexer(new StringReader(content));
        Symbol currentSymbol;
        do{
            currentSymbol = lexer.getNextSymbol();
            System.out.println(currentSymbol);
        } while(currentSymbol.getClass() != EndOfInputType.class);
    }

    /// Tests:

    /// Identifiers, types, values and assignment
    @Test
    public void test1() {
        String content = "final a int = 42;\n" +
                "var123 float = 3.14;\n" +
                "isOk bool = true;\n" +
                "message string = \"This is a simple message\";";
        printSymbols(content);
    }

    /// Record Identifiers, delimiters
    @Test
    public void test2() {
        String content = "Student rec {\n" +
                "grades int[];\n" +
                "followedCurses string[];\n" +
                "height float;\n" +
                "isMinor bool;\n" +
                "}";
        printSymbols(content);
    }

    /// Operators, keywords, delimiters
    /// Note: this code can't work without the preceding one, but
    /// it's not Lexer's work to verify this
    @Test
    public void test3() {
        String content = "fun isGraduating(Student stud) bool {\n" +
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
                "}";
        printSymbols(content);
    }

    /// More combinations of operators
    @Test
    public void test4() {
        String content = "fun main() {\n" +
                "aBool bool = true;\n" +
                "val1 int = 10;\n" +
                "val2 float = 8.99;\n" +
                "if (val1 > val2 * 2 && aBool || (val1 - val2) % 5 <= 100){\n" +
                "writeln(\"yes\");\n" +
                "}\n" +
                "else {\n" +
                "writeln(\"no\");\n" +
                "}\n" +
                "}";
        printSymbols(content);
    }

    /// Rest of keywords, operators and showing comments are ignored
    @Test
    public void test5() {
        String content = "fun main() {\n" +
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
                "}";
        printSymbols(content);
    }

    /// Field access and function call
    @Test
    public void test6() {
        String content = "Student rec {\n" +
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
                "}";
        printSymbols(content);
    }
}

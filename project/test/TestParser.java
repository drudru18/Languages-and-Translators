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
    public void test1() {
        Lexer lexer = new Lexer(new StringReader(
                "$ hello world \n"+
                        "fun main() { \n" +
                        "\n" +
                        "    value int = readInt(); \n" +
                        "\n" +
                        "    writeln(square(value)); \n" +
                        "\n" +
                        "    i int; \n" +
                        "\n" +
                        "    for (i, 1, 100, 1) { \n" +
                        "\n" +
                        "        while (value!=3) { \n" +
                        "\n" +
                        "            if (i > 10){ \n" +
                        "\n" +
                        "                $ .... \n" +
                        "\n" +
                        "            } else { \n" +
                        "\n" +
                        "                $ .... \n" +
                        "\n" +
                        "            } \n" +
                        "\n" +
                        "        } \n" +
                        "\n" +
                        "    } \n" +
                        "\n" +
                        "     \n" +
                        "\n" +
                        "    i = (i+2)*2; \n" +
                        "\n" +
                        "} "));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void test2() {
        Lexer lexer = new Lexer(new StringReader(
                "fun main() {\n" +
                        "    x int = 10;\n" +
                        "    \n" +
                        "    if (x > 5) {\n" +
                        "        writeln(\"Greater than 5\");\n" +
                        "    } else {\n" +
                        "        writeln(\"Less than or equal to 5\");\n" +
                        "    }\n" +
                        "}\n"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void test3() {
        Lexer lexer = new Lexer(new StringReader(
                "fun main() {\n" +
                        "    score int = readInt();\n" +
                        "\n" +
                        "    if (score >= 90) {\n" +
                        "        writeln(\"Grade: A\");\n" +
                        "    } else if (score >= 80) {\n" +
                        "        writeln(\"Grade: B\");\n" +
                        "    } else if (score >= 70) {\n" +
                        "        writeln(\"Grade: C\");\n" +
                        "    } else {\n" +
                        "        writeln(\"Grade: F\");\n" +
                        "    }\n" +
                        "}\n"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void test4() {
        Lexer lexer = new Lexer(new StringReader(
                "Person rec {\n" +
                        "    name string;\n" +
                        "    age int;\n" +
                        "}\n" +
                        "\n" +
                        "fun main() {\n" +
                        "    p Person = Person(\"Alice\", 25);\n" +
                        "    writeln(p.name);\n" +
                        "}\n"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

    @Test
    public void test5() {
        Lexer lexer = new Lexer(new StringReader(
                "fun sumArray(arr int[]) int {\n" +
                        "    total int = 0;\n" +
                        "    i int;\n" +
                        "    for (i, 0, len(arr), 1) {\n" +
                        "        total = total + arr[i];\n" +
                        "    }\n" +
                        "    return total;\n" +
                        "}\n" +
                        "\n" +
                        "fun main() {\n" +
                        "    nums int[] = array of [5];\n" +
                        "    nums[0] = 10;\n" +
                        "    nums[1] = 20;\n" +
                        "    nums[2] = 30;\n" +
                        "    nums[3] = 40;\n" +
                        "    nums[4] = 50;\n" +
                        "\n" +
                        "    writeln(sumArray(nums));\n" +
                        "}\n"));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
    }

}

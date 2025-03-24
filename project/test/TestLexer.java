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

public class TestLexer {

    public void printSymbols(String content) {
        Lexer lexer = new Lexer(new StringReader(content));
        Symbol currentSymbol;
        do{
            currentSymbol = lexer.getNextSymbol();
            System.out.println(currentSymbol);
        } while(currentSymbol.getClass() != EndOfInputType.class);
    }

    @Test
    public void testIdentifierVsRecordIdentifier() {
        String content = "var int = 3;\n" +
                "\n" +
                "Student rec{\n" +
                "   height float;\n" +
                "}\n" +
                "\n" +
                "var2 Student;";
        printSymbols(content);
    }

    @Test
    public void testDifferentOperators() {
        String content = "a int = 3;\n" +
                "print(a + 5 / 2 - 4 && 1 || 6 % 2 > 1 < 5 <= 6 >= 4);";
        printSymbols(content);
    }

    @Test
    public void testKeywords() {
        String content = "while(true) {\n" +
                "   if(false){\n" +
                "      final a int;\n" +
                "      rec fun for For array of Array Of [ ]\n" +
                "   }\n" +
                "}";
        printSymbols(content);
    }

    @Test
    public void testValues() {
        String content = "a int = -5;\n" +
                "b float = 3.4;\n" +
                "c bool = false;\n" +
                "d string = \"heeeee lo\";";
        printSymbols(content);
    }

    @Test
    public void testSomeDelimitersAndOperators() {
        String content = ",,.[];\n" +
                "<<= >>=";
        printSymbols(content);
    }

}

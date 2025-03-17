import static org.junit.Assert.assertNotNull;

import compiler.Lexer.Symbol;
import compiler.Lexer.Symbols.EndOfInputType;
import compiler.Parser.ASTNode;
import compiler.Parser.ASTPrinter;
import compiler.Parser.Parser;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import compiler.Lexer.Lexer;

public class TestLexer {
    
    @Test
    public void test() {
        /*String input = "var x float = 2.45;";
        try {
            Path filePath = Paths.get("../code_example2025.txt");
            input = Files.readString(filePath, StandardCharsets.UTF_8);
            //System.out.println(Arrays.toString(input.split("\t")));
            StringReader reader = new StringReader(input);
            //Lexer lexer = new Lexer(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        Lexer lexer = new Lexer(new StringReader("fun copyPoints(Point[] p) Point { \n" +
                "\n" +
                "    \n" +
                "    var x float = 5.34; \n" +
                "    x == y; \n" +
                "    return Point(p[0].x+p[1].x, p[0].y+p[1].y); \n" +
                "\n" +
                "} "));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        ASTPrinter.print(ast);
        /*
        Symbol currentSymbol;
        do{
            currentSymbol = lexer.getNextSymbol();
            if(currentSymbol == null){
                System.out.println("Null");
                break;
            }
            System.out.println(currentSymbol);
        }while(currentSymbol.getClass() != EndOfInputType.class);*/
    }

}

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import compiler.Lexer.Lexer;

public class TestLexer {
    
    @Test
    public void test() {
        String input = "var x float = 2.45;";
        try {
            Path filePath = Paths.get("../code_example2025.txt");
            input = Files.readString(filePath);
            StringReader reader = new StringReader(input);
            Lexer lexer = new Lexer(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //StringReader reader = new StringReader(input);
        //Lexer lexer = new Lexer(reader);
        //assertNotNull(lexer.getNextSymbol());
    }

}

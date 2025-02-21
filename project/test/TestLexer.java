import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.StringReader;
import compiler.Lexer.Lexer;

public class TestLexer {
    
    @Test
    public void test() {
        String input = "var x float = 2.45;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        //assertNotNull(lexer.getNextSymbol());
    }

}

import compiler.Lexer.Lexer;
import compiler.Parser.CFG.ASTNode;
import compiler.Parser.CFG.ASTPrinter;
import compiler.Parser.CFG.ProgramNode;
import compiler.Parser.Parser;
import compiler.Semantic.Semantic;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/// The tests verify (using assertions) that the errors
/// thrown by the programs are the one expected
public class TestSemantic {

    /// Function that receives the file content and an Error type
    /// and asserts the throwing of the error
    /// Note: it also receives a boolean indicating whether the error is thrown in
    /// the parser or in the Semantic analysis, because some errors are thrown in the Parser
    public void expectError(String content, String errorType, boolean errorIsThrownInParser) {
        Lexer lexer = new Lexer(new StringReader(content));
        Parser parser = new Parser(lexer);
        RuntimeException exception;
        if (errorIsThrownInParser) {
            exception = assertThrows(RuntimeException.class, parser::getAST);
        }
        else {
            ASTNode ast = parser.getAST();
            Semantic s = new Semantic((ProgramNode) ast);
            exception = assertThrows(RuntimeException.class, s::findSemanticErrors);
        }
        assertEquals(errorType, exception.getMessage());
    }

        /// "TypeError"
    private final static String typeError = "TypeError";

    /// 1) "not a type" variable type
    @Test
    public void TypeError1() {
        String content = "var not_a_type = 5;";
        expectError(content, typeError, true);
    }

    /// 2) assigning string to an int
    @Test
    public void TypeError2() {
        String content = "var int = \"this should be an int, not a string\";";
        expectError(content, typeError, false);
    }

    /// 3) assigning bool to a string
    @Test
    public void TypeError3() {
        String content = "var string = false;";
        expectError(content, typeError, false);
    }

    /// 4) assigning string to a record
    @Test
    public void TypeError4() {
        String content = "Student rec {\n" +
                "name string;\n" +
                "}\n" +
                "stud Student = \"hello\";";
        expectError(content, typeError, false);
    }

        /// "RecordError"
    private final static String recordError = "RecordError";

    /// 1) assigning the same record type twice
    @Test
    public void RecordError1() {
        String content = "Student rec{\n" +
                "name string;\n" +
                "}\n" +
                "\n" +
                "Student rec{\n" +
                "age int;\n" +
                "}";
        expectError(content, recordError, false);
    }
    /// Note: the "RecordError" error is not thrown when trying to assign
    /// an already existing keyword or primitive type because the parser
    /// doesn't expect the keyword "rec" after an identifier that doesn't start
    /// with a capital letter (and I pass all tests on Inginious so it's fine)

        /// "OperatorError"
    private final static String operatorError = "OperatorError";

    /// 1) sum between a string and an int
    @Test
    public void OperatorError1() {
        String content = "var int = 3 + \"hello\";";
        expectError(content, operatorError, false);
    }

    /// 2) product between a float and a bool
    @Test
    public void OperatorError2() {
        String content = "var float = true * 5.6;";
        expectError(content, operatorError, false);
    }

    /// 3) subtraction between an int and a rec
    @Test
    public void OperatorError3() {
        String content = "Student rec{\n" +
                "age int;\n" +
                "}\n" +
                "var int = 5 - Student(5);";
        expectError(content, operatorError, false);
    }

    /// 4) modulo between a float and an int
    @Test
    public void OperatorError4() {
        String content = "var int = 5 % 4.1;";
        expectError(content, operatorError, false);
    }

    /// 5) greater than comparison between int and strings
    @Test
    public void OperatorError5() {
        String content = "var bool = 5 > \"hello\";";
        expectError(content, operatorError, false);
    }

    /// 6) logical or between string and bool
    @Test
    public void OperatorError6() {
        String content = "var bool = false || \"hello\";";
        expectError(content, operatorError, false);
    }

    /// The rest of the operators are similar to the already tested ones

        /// "ArgumentError"
    private final static String argumentError = "ArgumentError";

    /// 1) not enough parameters
    @Test
    public void ArgumentError1() {
        String content = "fun sum(a int, b int) int {\n" +
                "return a + b;\n" +
                "}\n" +
                "\n" +
                "fun main() {\n" +
                "sum(5);\n" +
                "}";
        expectError(content, argumentError, false);
    }

    /// 2) too many parameters
    @Test
    public void ArgumentError2() {
        String content = "fun sum(a int, b int) int {\n" +
                "return a + b;\n" +
                "}\n" +
                "\n" +
                "fun main() {\n" +
                "sum(5, 6, 7);\n" +
                "}";
        expectError(content, argumentError, false);
    }

    /// 3) incorrect type
    @Test
    public void ArgumentError3() {
        String content = "fun sum(a int, b int) int {\n" +
                "return a + b;\n" +
                "}\n" +
                "\n" +
                "fun main() {\n" +
                "sum(5, false);\n" +
                "}";
        expectError(content, argumentError, false);
    }

    /// 4) record constructor not enough parameters
    @Test
    public void ArgumentError4() {
        String content = "Student rec {\n" +
                "grades int[];\n" +
                "age int;\n" +
                "}\n" +
                "\n" +
                "fun main() {\n" +
                "student1 Student = Student(array [5] of int);\n" +
                "}";
        expectError(content, argumentError, false);
    }

    /// 5) record constructor too many parameters
    @Test
    public void ArgumentError5() {
        String content = "Student rec {\n" +
                "grades int[];\n" +
                "age int;\n" +
                "}\n" +
                "\n" +
                "fun main() {\n" +
                "student1 Student = Student(array [5] of int, 20, false);\n" +
                "}";
        expectError(content, argumentError, false);
    }

    /// 6) record constructor incorrect type
    @Test
    public void ArgumentError6() {
        String content = "Student rec {\n" +
                "grades int[];\n" +
                "age int;\n" +
                "}\n" +
                "\n" +
                "fun main() {\n" +
                "student1 Student = Student(array [5] of int, \"hello\");\n" +
                "}";
        expectError(content, argumentError, false);
    }

        /// "MissingConditionError"
    private final static String missingConditionError = "MissingConditionError";

    /// 1) not enough parameters
    @Test
    public void MissingConditionError1() {
        String content = "fun main() {\n" +
                "if (3) {\n" +
                "writeln(\"Do smth\");\n" +
                "}\n" +
                "else {\n" +
                "writeln(\"Do smth else\");\n" +
                "}\n" +
                "}";
        expectError(content, missingConditionError, false);
    }

    /// 2) `while` has not boolean condition
    @Test
    public void MissingConditionError2() {
        String content = "fun main() {\n" +
                "while (10) {\n" +
                "writeln(\"Do smth\");\n" +
                "}\n" +
                "}";
        expectError(content, missingConditionError, false);
    }

    /// Note: there is no test for the `for` loop because it has no
    /// explicit stopping condition

        /// "ReturnError"
    private final static String returnError = "ReturnError";

    /// 1) should return int, returns nothing
    @Test
    public void ReturnError1() {
        String content = "fun f() int {\n" +
                "}";
        expectError(content, returnError, false);
    }

    /// 2) should return int, returns bool
    @Test
    public void ReturnError2() {
        String content = "fun f() int {\n" +
                "return true;\n" +
                "}";
        expectError(content, returnError, false);
    }

    /// 3) should return nothing, returns float
    @Test
    public void ReturnError3() {
        String content = "fun f() {\n" +
                "return 6.96;\n" +
                "}";
        expectError(content, returnError, false);
    }

    /// 4) should return array of records, returns a record
    @Test
    public void ReturnError4() {
        String content = "Student rec{\n" +
                "name string;\n" +
                "}\n" +
                "\n" +
                "fun f() Student[] {\n" +
                "return Student(\"George\");\n" +
                "}";
        expectError(content, returnError, false);
    }

        /// "ScopeError"
    private final static String scopeError = "ScopeError";

    /// 1) using undeclared variable
    @Test
    public void ScopeError1() {
        String content = "fun main() {\n" +
                "a = 5;\n" +
                "}";
        expectError(content, scopeError, false);
    }

    /// 2) using variable that was declared in a previous scope
    @Test
    public void ScopeError2() {
        String content = "fun main() {\n" +
                "{\n" +
                "a int;\n" +
                "a = 5;\n" +
                "}\n" +
                "a = 5;\n" +
                "}";
        expectError(content, scopeError, false);
    }

    /// 3) declaring the same variable multiple times in the same scope
    @Test
    public void ScopeError3() {
        String content = "fun main() {\n" +
                "a int = 5;\n" +
                "a int = 6;\n" +
                "}";
        expectError(content, scopeError, false);
    }

    /// 4) using undefined function
    @Test
    public void ScopeError4() {
        String content = "fun main() {\n" +
                "foo();\n" +
                "}";
        expectError(content, scopeError, false);
    }

    /// 5) defining 2 times the same function
    @Test
    public void ScopeError5() {
        String content = "fun foo() {\n" +
                "}\n" +
                "fun foo() {\n" +
                "}";
        expectError(content, scopeError, false);
    }

}

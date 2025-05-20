import compiler.CodeGeneration.CodeGeneration;
import compiler.Lexer.Lexer;
import compiler.Parser.CFG.ASTNode;
import compiler.Parser.CFG.ASTPrinter;
import compiler.Parser.CFG.ProgramNode;
import compiler.Parser.Parser;
import compiler.Semantic.Semantic;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/// For this last phase, the tests are programs that prove
/// the code generation (and therefore the compiler) properly
/// process the input file source code and produce the correct
/// output
/// Note: for the input reading functionality from stdin, the tests are commented
/// and can be tested by uncommenting them so they don't block the thread
/// by waiting for input
public class TestCodeGeneration {

    /// Function that receives the file content and an Error type
    /// and asserts the throwing of the error
    /// Note: it also receives a boolean indicating whether the error is thrown in
    /// the parser or in the Semantic analysis, because some errors are thrown in the Parser
    public void compile(String content, String outputPath) throws IOException {
        Lexer lexer = new Lexer(new StringReader(content));
        Parser parser = new Parser(lexer);
        ASTNode ast = parser.getAST();
        Semantic s = new Semantic((ProgramNode) ast);
        s.findSemanticErrors();
        CodeGeneration cg = new CodeGeneration((ProgramNode) ast, outputPath);
        cg.codeGeneration();
    }

    /// Function that executes the main file class and prints the output
    public void execute(String outputFolder, String outputFile) throws IOException, InterruptedException {
        // Set the correct path to where Java is on your machine (you, the code reviewer) to make this work
        // Apparently Intellij doesn't recognize "java"...
        String javaPath = "/usr/lib/jvm/java-17-openjdk-amd64/bin/java"; // (to modify)
        ProcessBuilder builder = new ProcessBuilder(
                javaPath, "-cp", outputFolder, outputFile
        );

        Process process = builder.start();

        // Capture stdout
        BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        // Capture stderr
        BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        System.out.println("Standard Output:");
        String line;
        while ((line = stdOut.readLine()) != null) {
            System.out.println(line);
        }

        System.out.println("\nError Output:");
        while ((line = stdErr.readLine()) != null) {
            System.err.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("\nExited with code: " + exitCode);
    }

    /// Function that executes the main file class and prints the output
    public void executeWithInput(String outputFolder, String outputFile, String inputText) throws IOException, InterruptedException {
        // Set the correct path to where Java is on your machine (you, the code reviewer) to make this work
        // Apparently Intellij doesn't recognize "java"...
        String javaPath = "/usr/lib/jvm/java-17-openjdk-amd64/bin/java"; // (to modify)
        ProcessBuilder builder = new ProcessBuilder(
                javaPath, "-cp", outputFolder, outputFile
        );

        Process process = builder.start();

        // Write simulated input to the child process
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(inputText);
            writer.flush();
        }

        // Capture stdout
        BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        // Capture stderr
        BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        System.out.println("Standard Output:");
        String line;
        while ((line = stdOut.readLine()) != null) {
            System.out.println(line);
        }

        System.out.println("\nError Output:");
        while ((line = stdErr.readLine()) != null) {
            System.err.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("\nExited with code: " + exitCode);
    }

    /// Function that receives the file content and an Error type
    /// and asserts the throwing of the error
    public void showError(String content) {
        Lexer lexer = new Lexer(new StringReader(content));
        Parser parser = new Parser(lexer);
        RuntimeException exception;
        exception = assertThrows(RuntimeException.class, parser::getAST);
        System.out.println(exception.getMessage());
    }

    private static final String commonPath = "src/main/java/compiler/CodeGeneration/testFiles/";

    /// Tests a program that computes averages of students' grades and
    /// prints the names of those whose average is greater or equal than the
    /// minimum required grade.
    /// The source code can be found in: "src/main/java/compiler/CodeGeneration/testFiles/students_grading.lang"
    /// and the .class files in: "students_grading/" (at the root of the project)
    @Test
    public void test_students_grading() throws IOException, InterruptedException {
        String mainClassName = "students_grading";
        String fileName = mainClassName + ".lang";
        String content = Files.readString(Paths.get(commonPath + fileName));
        compile(content, mainClassName + "/" + mainClassName + ".class");
        execute(mainClassName + "/", mainClassName);
    }

    /// Tests a program that verifies the simple math conjectures which says that whatever natural number
    /// pick, while the number is different of 1, if it's odd multiply it by 3 and add 1 and if it's even
    /// divide it by 2, you will always end up to 1 at some point.
    /// This test asks you to choose a number in the variable `yourNumber` that will be given as input in stdin
    /// to the program launched to test the scan functionality
    /// The source code can be found in: "src/main/java/compiler/CodeGeneration/testFiles/simple_math_conjecture.lang"
    /// and the .class files in: "simple_math_conjecture/" (at the root of the project)
    @Test
    public void test_simple_math_conjecture() throws IOException, InterruptedException {
        String mainClassName = "simple_math_conjecture";
        String fileName = mainClassName + ".lang";
        String content = Files.readString(Paths.get(commonPath + fileName));
        compile(content, mainClassName + "/" + mainClassName + ".class");
        // Choose the number you want to run the program with (default 7)
        String yourNumber = "7";
        executeWithInput(mainClassName + "/", mainClassName, yourNumber);
    }

    /// Tests a program that recursively computes the factorial of a number you choose as input
    /// This test asks you to choose a number in the variable `yourNumber` that will be given as input in stdin
    /// to the program launched to test the scan functionality
    /// The source code can be found in: "src/main/java/compiler/CodeGeneration/testFiles/recursion_factorial.lang"
    /// and the .class files in: "recursion_factorial/" (at the root of the project)
    @Test
    public void test_recursion_factorial() throws IOException, InterruptedException {
        String mainClassName = "recursion_factorial";
        String fileName = mainClassName + ".lang";
        String content = Files.readString(Paths.get(commonPath + fileName));
        compile(content, mainClassName + "/" + mainClassName + ".class");
        // Choose the number you want to run the program with (default 7)
        String yourNumber = "7";
        executeWithInput(mainClassName + "/", mainClassName, yourNumber);
    }

    /// Tests a program that prints a countdown starting from a value, but only prints 1 value out of 2
    /// to show the usage of booleans and of the `!` (not) unary operator
    /// This test asks you to choose a number in the variable `yourNumber` that will be given as input in stdin
    /// to the program launched to test the scan functionality
    /// The source code can be found in: "src/main/java/compiler/CodeGeneration/testFiles/countdown_with_redlight.lang"
    /// and the .class files in: "countdown_with_redlight/" (at the root of the project)
    @Test
    public void test_countdown_with_redlight() throws IOException, InterruptedException {
        String mainClassName = "countdown_with_redlight";
        String fileName = mainClassName + ".lang";
        String content = Files.readString(Paths.get(commonPath + fileName));
        compile(content, mainClassName + "/" + mainClassName + ".class");
        // Choose the number you want to run the program with (default 20)
        String yourNumber = "20";
        executeWithInput(mainClassName + "/", mainClassName, yourNumber);
    }

            /// Extra features tests

        /// Content comparison  between strings

    /// Program that shows the difference between a reference
    /// and content comparison between strings
    /// The source code can be found in: "src/main/java/compiler/CodeGeneration/testFiles/string_comparing_content_vs_reference.lang"
    /// and the .class files in: "string_comparing_content_vs_reference/" (at the root of the project)
    @Test
    public void test_string_comparing_content_vs_reference() throws IOException, InterruptedException {
        String mainClassName = "string_comparing_content_vs_reference";
        String fileName = mainClassName + ".lang";
        String content = Files.readString(Paths.get(commonPath + fileName));
        compile(content, mainClassName + "/" + mainClassName + ".class");
        execute(mainClassName + "/", mainClassName);
    }

        /// More complex syntax errors thrown by the Parser

    /// In the global scope, only variable, function and record declarations are
    /// allowed, therefore any non-allowed statement will throw this error
    @Test
    public void test_not_allowed_outside_error_1() {
        String content = "if (4 > 3) {\n" +
                "writeln(\"Yes\");\n" +
                "}";
        showError(content);
    }
    @Test
    public void test_not_allowed_outside_error_2() {
        String content = "var int = 5;\n" +
                "while (var >= 0) {\n" +
                "var = var-1;\n" +
                "writeln(\"One more line\");\n" +
                "}";
        showError(content);
    }
    @Test
    public void test_not_allowed_outside_error_3() {
        String content = "var int;\n" +
                "for (var, 5, 0, -1) {\n" +
                "writeln(\"One more line\");\n" +
                "}";
        showError(content);
    }

    /// After the symbol `.`, there must be a field name (if the `.` is not part of
    /// a float number of course
    @Test
    public void test_field_expected_1() {
        String content = "Student rec {\n" +
                "name string;\n" +
                "}\n" +
                "\n" +
                "fun main() {\n" +
                "student Student = Student(\"John\");\n" +
                "writeln(student.Student);\n" +
                "}";
        showError(content);
    }

    /// After the symbol `final`, there must be a variable name, and it prints
    /// the symbol it received
    @Test
    public void test_expected_variable_name_1() {
        String content = "final 55 int = 12;";
        showError(content);
    }

    /// After the symbol `fun`, there must be a function name, and it prints
    /// the symbol it received
    @Test
    public void test_expected_function_name_1() {
        String content = "fun while(a int, b int) int {\n" +
                "return a + b;\n" +
                "}";
        showError(content);
    }

    /// Inside the declaration of a function, the parameters names are expected
    /// to be variable names
    @Test
    public void test_expected_parameter_name_1() {
        String content = "fun sum(a int, for int) int {\n" +
                "return a + int;\n" +
                "}";
        showError(content);
    }

    /// After the keyword `free`, it is expected to see a variable name
    @Test
    public void test_expected_var_after_free_1() {
        String content = "fun main() {\n" +
                "arr int[] = array [10] of int;\n" +
                "arr[6] = 42;\n" +
                "free bool;\n" +
                "}";
        showError(content);
    }

    /// Not closing brackets, parenthesis
    @Test
    public void test_close_1() {
        String content = "arr int[] = array [5 of int;";
        showError(content);
    }
    @Test
    public void test_close_2() {
        String content = "fun main() {\n" +
                "if (6 > 4 {\n" +
                "writeln(\"It's bigger\");\n" +
                "}\n" +
                "}";
        showError(content);
    }
    @Test
    public void test_close_3() {
        String content = "fun sum(a int b int) int {\n" +
                "return a + int;\n" +
                "}";
        showError(content);
    }
}

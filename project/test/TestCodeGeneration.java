import compiler.CodeGeneration.CodeGeneration;
import compiler.Lexer.Lexer;
import compiler.Parser.CFG.ASTNode;
import compiler.Parser.CFG.ASTPrinter;
import compiler.Parser.CFG.ProgramNode;
import compiler.Parser.Parser;
import compiler.Semantic.Semantic;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
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
        try {
            Process process = Runtime.getRuntime().exec("/usr/lib/jvm/java-17-openjdk-amd64/bin/java -cp " + outputFolder + " " + outputFile);

            // Capture stdout
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // Capture stderr
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Print stdout
            String line;
            System.out.println("Standard Output:");
            while ((line = stdOut.readLine()) != null) {
                System.out.println(line);
            }

            // Print stderr
            System.out.println("\nError Output:");
            while ((line = stdErr.readLine()) != null) {
                System.err.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("\nExited with code: " + exitCode);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final String commonPath = "src/main/java/compiler/CodeGeneration/testFiles/";

    /// Tests a program that computes averages of students' grades and
    /// prints the names of those whose average is greater or equal than the
    /// minimum required grade.
    /// The source code can be found in: "src/main/java/compiler/CodeGeneration/testFiles/students_grading.lang"
    /// and the .class files in: "students_grading/" (at the root of the project)
    @Test
    public void test1() throws IOException, InterruptedException {
        String fileName = "students_grading.lang";
        String content = Files.readString(Paths.get(commonPath + fileName));
        compile(content, "students_grading/students_grading.class");
        execute("students_grading/", "students_grading");
    }


}

import br.compiler.compiler.CompilationEngine;
import br.compiler.factory.ParserFactory;
import br.compiler.model.CompilationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CompilerSyntaxTests {

    private CompilationEngine compiler;

    @BeforeEach
    void setup() {
        compiler = ParserFactory.createCompilationEngine();
    }

    // adjust to your actual compiler entry point

    // ------------------------------------------------------------
    // 1. FULL INTEGRATION TEST (your entire program)
    // ------------------------------------------------------------
    @Test
    void shouldCompileFullStressProgram() {

        String program = """
        /**/
        /* */

        /* TESTE */

        int a = 1212;

        /*****
        asdadasas
        ****/

        // Variable declarations
        int a, b = 10, c;
        float x = 1.5, y, z = 3.14;
        bool flag1, flag2 = true;

        int arr[5];
        int arr2[] = {1, 2, 3};
        string names[] = {"Ana", "Joao", "Maria"};

        // Simple conditional (if only)
        int x = 10;

        if (x > 5) {
            write("x is greater than 5");
        }

        // Compound conditional (if + else)
        int x = 3;

        if (x > 5) {
            write("greater");
        } else {
            write("smaller or equal");
        }

        // While loop (pre-tested)
        int i = 0;

        while (i < 5) {
            write(i);
            i = i + 1;
        }

        // For loop
        for (int i = 0; i < 10; i = i + 1) {
            write(i);
        }

        // Do-while loop
        int i = 0;

        do {
            write(i);
            i = i + 1;
        } while (i < 5);

        // Input
        int a, b;
        int arr[3];

        read(a, b);
        read(arr[0], arr[1], arr[2]);

        // Output
        int x = 10;
        int arr[] = {1, 2, 3};

        write("Value of x:", x);
        write("Array:", arr[0], arr[1], arr[2]);
        write("Done");

        // Assignments
        int a = 5;
        int b = 10;
        int arr[3];

        a = b + 2 * 3;
        arr[0] = a + b;
        arr[1] += 5;
        arr[2] = arr[0] * arr[1];

        // Functions
        void printSum(int a, int b) {
            write(a + b);
        }

        int sum(int a, int b) {
            return a + b;
        }

        int main() {
            int result;
            result = sum(5, 10);
            printSum(result, 20);
        }

        int sumArray(int arr[]) {
            int i = 0;
            int total = 0;

            while (i < 3) {
                total = total + arr[i];
                i = i + 1;
            }

            return total;
        }

        int main() {
            int arr[] = {1, 2, 3};
            int result;

            result = sumArray(arr);
            write("Sum:", result);
        }

        int global = 10;

        int multiply(int a, int b) {
            return a * b;
        }

        void main() {
            int i, result;
            int arr[] = {1, 2, 3};

            read(i);

            for (int j = 0; j < 3; j = j + 1) {
                result = multiply(arr[j], i);

                if (result > 10) {
                    write("Big:", result);
                } else {
                    write("Small:", result);
                }
            }

            do {
                i = i - 1;
                write(i);
            } while (i > 0);
        }
        """;

        CompilationResult result = compiler.compile(program);

        assertTrue(result.isSuccess(),
                () -> "Expected successful compilation but got error:\n" + result);
    }

    // ------------------------------------------------------------
    // 2. COMMENT TESTS
    // ------------------------------------------------------------
    @Test
    void shouldHandleBlockAndLineComments() {
        String program = """
        /**/
        /* comment */
        // line comment
        int a = 1;
        """;

        CompilationResult result = compiler.compile(program);

        assertTrue(result.isSuccess(), result::toString);
    }

    // ------------------------------------------------------------
    // 3. VARIABLE DECLARATIONS TEST
    // ------------------------------------------------------------
    @Test
    void shouldParseVariableDeclarations() {
        String program = """
        int a, b = 10, c;
        float x = 1.5, y, z = 3.14;
        bool f = true;
        string s = "hello";
        """;

        CompilationResult result = compiler.compile(program);

        assertTrue(result.isSuccess(), result::toString);
    }

    // ------------------------------------------------------------
    // 4. CONTROL STRUCTURES TEST
    // ------------------------------------------------------------
    @Test
    void shouldParseControlStructures() {
        String program = """
        int x = 10;

        if (x > 5) {
            x = x + 1;
        } else {
            x = x - 1;
        }

        while (x > 0) {
            x = x - 1;
        }

        do {
            x = x + 1;
        } while (x < 10);

        for (int i = 0; i < 10; i = i + 1) {
            x = x + i;
        }
        """;

        CompilationResult result = compiler.compile(program);

        assertTrue(result.isSuccess(), result::toString);
    }

    // ------------------------------------------------------------
    // 5. FUNCTIONS TEST
    // ------------------------------------------------------------
    @Test
    void shouldParseFunctionsAndCalls() {
        String program = """
        int sum(int a, int b) {
            return a + b;
        }

        void print(int x) {
            write(x);
        }

        int main() {
            int r;
            r = sum(1, 2);
            print(r);
        }
        """;

        CompilationResult result = compiler.compile(program);

        assertTrue(result.isSuccess(), result::toString);
    }

    // ------------------------------------------------------------
    // 6. FAILURE TEST (IMPORTANT FOR DEBUGGING)
    // ------------------------------------------------------------
    @Test
    void shouldFailOnInvalidSyntax() {
        String program = """
        int a = ;
        """;

        CompilationResult result = compiler.compile(program);

        assertFalse(result.isSuccess(),
                "Expected compilation failure for invalid syntax");
    }
}
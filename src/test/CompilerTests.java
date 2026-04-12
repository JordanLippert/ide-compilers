import br.compiler.compiler.CompilationEngine;
import br.compiler.factory.ParserFactory;
import br.compiler.model.CompilationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompilerTests {

    private CompilationEngine engine;

    @BeforeEach
    void setup() {
        engine = ParserFactory.createCompilationEngine();
    }

    private void assertSuccess(String code) {
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Expected success but failed. Error type: " + result.getErrorType() + ". Error message: " + result.getErrorMessage() + ". Line: " + result.getErrorLine() + ". Position: " + result.getErrorPosition());
    }

    private void assertFailure(String code) {
        CompilationResult result = engine.compile(code);
        assertFalse(result.isSuccess(), "Expected failure but succeeded");
    }

    // Declaração de variáveis
    @Test
    void testVariableDeclaration() {
        String code = """
            int a;
            string teste = "oi";
            int b, c;
        """;
        assertSuccess(code);
    }

    // Declaração de arrays (vetores)
    @Test
    void testArrayDeclaration() {
        String code = """
            int va[11], vb[17];
            int a[5] = {1,2,3};
            char str[] = {"abc"};
        """;
        assertSuccess(code);
    }

    // Desvio condicional simples (somente if)
    @Test
    void testSimpleIf() {
        String code = """
            int a = 50;
            
            write("antes do if");
            
            if (a > 10) {
                write("a é maior");
            }
            
            if (a > 100) {
                write("a é menor");
            }
            
            write("fora do if");
        """;
        assertSuccess(code);
    }

    // Desvio condicional composto (com else)
    @Test
    void testIfElse() {
        String code = """
            int a = -1;
            if (a > 0) {
                write("a é maior que zero");
            } else {
                write("a é menor que zero");
            }
        """;
        assertSuccess(code);
    }

    // 4. Laço pré-testado (while)
    @Test
    void testWhileLoop() {
        String code = """
            int a;
            while (a < 10) {
                a = a + 1;
            }
        """;
        assertSuccess(code);
    }

    // 4. Laço com variável de controle (for)
    @Test
    void testForLoop() {
        String code = """
            int i;
            for (i = 0; i < 10; i = i + 1) {
                i = i;
            }
        """;
        assertSuccess(code);
    }

    // 4. Laço pós-testado (do-while)
    @Test
    void testDoWhileLoop() {
        String code = """
            int a;
            do {
                a = a + 1;
            } while (a < 10);
        """;
        assertSuccess(code);
    }

    // 5. Entrada de dados
    @Test
    void testInput() {
        String code = """
            int a;
            leia(a);
        """;
        assertSuccess(code);
    }

    // 6. Saída de dados
    @Test
    void testOutput() {
        String code = """
            int a;
            escreva(a);
            escreva("hello");
        """;
        assertSuccess(code);
    }

    // 7. Atribuições com expressões
    @Test
    void testAssignments() {
        String code = """
            int a, b;
            a = 5;
            b = a + 10 * (2 + 3);
        """;
        assertSuccess(code);
    }

    // 8. Sub-rotinas (função)
    @Test
    void testFunctionDefinitionAndCall() {
        String code = """
            int soma(int a, int b) {
                return a + b;
            }

            int x;
            x = soma(1, 2);
        """;
        assertSuccess(code);
    }

    // 8. Procedimento
    @Test
    void testProcedure() {
        String code = """
            void proc(int a) {
                a = a + 1;
            }

            int x = 1;
            proc(x);
        """;
        assertSuccess(code);
    }

    // 9. Expressões completas
    @Test
    void testComplexExpressions() {
        String code = """
            int b = 2, c = 10;
            float a = (b + c) * (a - 1) / 2;
            if ((a > b) && (c < 10) || (a == c)) {
                a = 0;
            }
        """;
        assertSuccess(code);
    }

    // ❌ Teste de erro sintático
    @Test
    void testSyntaxError() {
        String code = """
            int a
            a = 10;
        """;
        assertFailure(code);
    }
}
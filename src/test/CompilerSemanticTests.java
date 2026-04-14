import compiler.compiler.CompilationEngine;
import compiler.factory.ParserFactory;
import compiler.model.CompilationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompilerSemanticTests {

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

    // Saída de dados
    @Test
    void testOutput() {
        String code = """
            int a;
            write(a);
            write("hello");
        """;
        assertSuccess(code);
    }

    // Declaração de variáveis
    @Test
    void testVariableDeclaration() {
        String code = """
            int a;
            string teste;
            int b, c, d;
            
            a = 100;
            teste = "oi";
            d = 10;
            
            write(teste);
            write(d);
            write(a);
            write(b);
        """;
        assertSuccess(code);
    }

    // Declaração de arrays (vetores)
    @Test
    void testArrayDeclaration() {
        String code = """
            int va[11], vb[17];
            int a[5] = {1,2,3};
            char str[] = {'a', 'b', 'c'};
        """;
        assertSuccess(code);
    }

    // Entrada de dados
    @Test
    void testInput() {
        String code = """
            int a;
            read(a);
        """;
        assertSuccess(code);
    }

    // Expressões aritmeticas

    // Expressoes relacionais

    // Expressoes logicas

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

    // 8. Sub-rotinas (função)
    @Test
    void testFunction() {
        String code = """
            int soma(int a, int b) {
                return a + b;
            }

            int x;
            x = soma(1, 2);
            write(x);
        """;
        assertSuccess(code);
    }

    // 8. Procedimento
    @Test
    void testProcedure() {
        String code = """
            void proc(int a) {
                a = a + 1;
                write(a);
            }

            int x = 1;
            proc(x);
        """;
        assertSuccess(code);
    }
}
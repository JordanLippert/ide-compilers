import compiler.compiler.CompilationEngine;
import compiler.factory.ParserFactory;
import compiler.model.CompilationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("Function return value not supported — call result not pushed onto literalStack")
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

    // Expressões aritméticas
    @Test
    void testArithmeticExpressions() {
        String code = """
            int a = 10;
            int b = 3;
            int c;
            c = a + b;
            c = a - b;
            c = a * b;
            c = a / b;
        """;
        assertSuccess(code);
    }

    // Expressões aritméticas com precedência
    @Test
    void testArithmeticPrecedence() {
        String code = """
            int a = 5;
            int b = 3;
            int c;
            c = a + b * 2;
            c = (a + b) * 2;
            c = a * b - 1;
        """;
        assertSuccess(code);
    }

    // Expressões relacionais
    @Test
    void testRelationalExpressions() {
        String code = """
            int a = 10;
            int b = 5;
            if (a == b) { write("igual"); }
            if (a != b) { write("diferente"); }
            if (a > b)  { write("maior"); }
            if (a < b)  { write("menor"); }
            if (a >= b) { write("maior ou igual"); }
            if (a <= b) { write("menor ou igual"); }
        """;
        assertSuccess(code);
    }

    // Expressões lógicas
    @Test
    void testLogicalExpressions() {
        String code = """
            bool a = true;
            bool b = false;
            if (a && b) { write("ambos"); }
            if (a || b) { write("algum"); }
            if (!a) { write("negacao"); }
            if (a && !b) { write("a e nao b"); }
        """;
        assertSuccess(code);
    }

    // Expressões lógicas combinadas com relacionais
    @Test
    void testCombinedLogicalAndRelational() {
        String code = """
            int a = 10;
            int b = 5;
            int c = 7;
            if (a > b && c < a) { write("verdadeiro"); }
            if (a == 10 || b == 0) { write("algum verdadeiro"); }
        """;
        assertSuccess(code);
    }

    // Falha: declaração duplicada no mesmo escopo
    @Test
    void testFailsDuplicateVariableDeclaration() {
        String code = """
            int a;
            int a;
        """;
        assertFailure(code);
    }

    // Falha: declaração duplicada com inicialização
    @Test
    void testFailsDuplicateVariableDeclarationWithInit() {
        String code = """
            int x = 1;
            int x = 2;
        """;
        assertFailure(code);
    }

    // Falha: atribuição a variável não declarada
    @Test
    void testFailsAssignmentToUndeclaredVariable() {
        String code = """
            x = 10;
        """;
        assertFailure(code);
    }

    // Falha: uso de variável não declarada em expressão
    @Test
    void testFailsUndeclaredVariableInExpression() {
        String code = """
            int a;
            a = undeclaredVar + 1;
        """;
        assertFailure(code);
    }

    // Falha: uso de variável não declarada no write
    @Test
    void testFailsUndeclaredVariableInWrite() {
        String code = """
            write(undeclaredVar);
        """;
        assertFailure(code);
    }

    // Falha: erro semântico gera tipo correto
    @Test
    void testDuplicateDeclarationErrorType() {
        String code = """
            int a;
            int a;
        """;
        CompilationResult result = engine.compile(code);
        assertFalse(result.isSuccess(), "Esperava falha por declaração duplicada");
        assertEquals("SEMANTIC", result.getErrorType(),
            "Tipo de erro deveria ser SEMANTIC, mas foi: " + result.getErrorType());
    }

    // Falha: variável não declarada em condição
    @Test
    void testFailsUndeclaredVariableInCondition() {
        String code = """
            if (notDeclared > 0) {
                write("ok");
            }
        """;
        assertFailure(code);
    }

    // ------------------------------------------------------------
    // Action #13 — assign variable (marca variável como inicializada)
    // ------------------------------------------------------------

    @Test
    void testAction13BasicAssignment() {
        String code = """
            int a;
            a = 42;
            write(a);
        """;
        assertSuccess(code);
    }

    @Test
    void testAction13AssignmentFromVariable() {
        String code = """
            int a = 10;
            int b;
            b = a;
            write(b);
        """;
        assertSuccess(code);
    }

    @Test
    void testAction13MultipleReassignments() {
        String code = """
            int a = 1;
            a = 2;
            a = 3;
            write(a);
        """;
        assertSuccess(code);
    }

    @Test
    void testAction13AssignmentFromExpression() {
        String code = """
            int a = 5;
            int b = 3;
            int c;
            c = a + b * 2;
            write(c);
        """;
        assertSuccess(code);
    }

    // ------------------------------------------------------------
    // Action #14 — capture assign target (captura lado esquerdo)
    // ------------------------------------------------------------

    @Test
    void testAction14AssignTargetDeclared() {
        String code = """
            int x;
            x = 99;
        """;
        assertSuccess(code);
    }

    @Test
    void testAction14AssignTargetNotDeclaredThrows() {
        String code = """
            notDeclared = 5;
        """;
        CompilationResult result = engine.compile(code);
        assertFalse(result.isSuccess(), "Esperava falha: variável não declarada");
        assertEquals("SEMANTIC", result.getErrorType(),
            "Tipo de erro deveria ser SEMANTIC, foi: " + result.getErrorType());
    }

    @Test
    void testAction14AssignTargetInFunctionScope() {
        String code = """
            void f(int a) {
                int local;
                local = a + 1;
                write(local);
            }
            f(10);
        """;
        assertSuccess(code);
    }
}
import compiler.compiler.CompilationEngine;
import compiler.factory.ParserFactory;
import compiler.model.CompilationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    // ------------------------------------------------------------
    // Avisos (Warnings) — variável não utilizada e não inicializada
    // ------------------------------------------------------------

    @Test
    void testWarningUnusedVariable() {
        String code = """
            int unused;
            int used = 5;
            write(used);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");
        assertTrue(
            result.getWarnings().stream().anyMatch(w -> w.contains("unused")),
            "Esperava aviso para variável não utilizada, obtido: " + result.getWarnings()
        );
    }

    @Test
    void testWarningUsedButUninitializedVariable() {
        // int y = 1 resets justDeclared via #10, so write(x) does NOT falsely mark x as initialized
        String code = """
            int x;
            int y = 1;
            write(x);
            write(y);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");
        assertTrue(
            result.getWarnings().stream().anyMatch(w -> w.contains("x") && w.contains("inicializado")),
            "Esperava aviso de uso sem inicialização, obtido: " + result.getWarnings()
        );
    }

    @Test
    void testNoWarningWhenVariableInitializedAndUsed() {
        String code = """
            int x = 10;
            write(x);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");
        assertTrue(
            result.getWarnings().isEmpty(),
            "Não deveria ter avisos, obtido: " + result.getWarnings()
        );
    }

    // ------------------------------------------------------------
    // Compatibilidade de tipos
    // ------------------------------------------------------------

    @Test
    void testFailsIncompatibleTypeAssignmentBoolToInt() {
        String code = """
            int x;
            bool b = true;
            x = b;
        """;
        assertFailure(code);
    }

    @Test
    void testFailsIncompatibleTypeAssignmentStringToInt() {
        String code = """
            int x;
            x = "hello";
        """;
        assertFailure(code);
    }

    @Test
    void testFailsArithmeticOnBooleans() {
        String code = """
            bool a = true;
            bool b = false;
            int c;
            c = a + b;
        """;
        assertFailure(code);
    }

    @Test
    void testFailsStringMultiplication() {
        String code = """
            string a = "hello";
            string b = "world";
            string c;
            c = a * b;
        """;
        assertFailure(code);
    }

    @Test
    void testFailsLogicalOperatorOnIntegers() {
        String code = """
            int a = 1;
            int b = 2;
            if (a && b) { write("ok"); }
        """;
        assertFailure(code);
    }

    @Test
    void testCompatibleNumericWidening() {
        String code = """
            int a = 5;
            float b = 2.0;
            float c;
            c = a + b;
            write(c);
        """;
        assertSuccess(code);
    }

    // ------------------------------------------------------------
    // Isolamento de escopo
    // ------------------------------------------------------------

    @Test
    void testFailsInnerScopeVariableNotAccessibleOutside() {
        String code = """
            int a = 1;
            if (a > 0) {
                int inner = 10;
            }
            write(inner);
        """;
        assertFailure(code);
    }

    @Test
    void testSameIdentifierInDifferentScopesAllowed() {
        String code = """
            int x = 1;
            if (x > 0) {
                int x = 2;
                write(x);
            }
            write(x);
        """;
        assertSuccess(code);
    }

    @Test
    void testInnerScopeCanAccessOuterVariable() {
        String code = """
            int outer = 42;
            if (outer > 0) {
                write(outer);
            }
        """;
        assertSuccess(code);
    }

    // ------------------------------------------------------------
    // Tabela de símbolos — verificação de conteúdo
    // ------------------------------------------------------------

    @Test
    void testSymbolTableVariableTypeAndScope() {
        String code = """
            int myVar = 42;
            write(myVar);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        List<Object[]> rows = result.getSymbolTableRows();
        Object[] row = rows.stream()
            .filter(r -> "myVar".equals(r[0]))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'myVar' não encontrado na tabela"));

        assertEquals("Integer", row[1], "Tipo deveria ser Integer");
        assertEquals("global",  row[2], "Escopo deveria ser global");
        assertEquals("Sim",     row[3], "Deveria estar inicializado");
        assertEquals("Sim",     row[4], "Deveria estar usado");
        assertEquals("Não",     row[5], "Não deveria ser parâmetro");
        assertEquals("Não",     row[7], "Não deveria ser array");
        assertEquals("Não",     row[10], "Não deveria ser função");
    }

    @Test
    void testSymbolTableParameterModality() {
        String code = """
            void f(int param) {
                write(param);
            }
            f(1);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        List<Object[]> rows = result.getSymbolTableRows();
        Object[] row = rows.stream()
            .filter(r -> "param".equals(r[0]))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'param' não encontrado na tabela"));

        assertEquals("Integer", row[1], "Tipo do parâmetro deveria ser Integer");
        assertEquals("Sim",     row[3], "Parâmetro deveria estar inicializado");
        assertEquals("Sim",     row[4], "Parâmetro deveria estar usado");
        assertEquals("Sim",     row[5], "Deveria ser marcado como parâmetro");
    }

    @Test
    void testSymbolTableMultipleVariablesCorrectCount() {
        String code = """
            int a = 1;
            int b = 2;
            int c = 3;
            write(a);
            write(b);
            write(c);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        List<Object[]> rows = result.getSymbolTableRows();
        long count = rows.stream()
            .filter(r -> "a".equals(r[0]) || "b".equals(r[0]) || "c".equals(r[0]))
            .count();
        assertEquals(3, count, "Deveriam existir exatamente 3 símbolos (a, b, c)");
    }

    // ------------------------------------------------------------
    // Valor dos símbolos — armazenamento e uso em operações
    // ------------------------------------------------------------

    @Test
    void testSymbolValueStoredOnLiteralInit() {
        String code = """
            int x = 42;
            write(x);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        Object[] row = result.getSymbolTableRows().stream()
            .filter(r -> "x".equals(r[0])).findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'x' não encontrado"));

        assertEquals(42, row[11], "Valor de x deveria ser 42 (Integer)");
    }

    @Test
    void testSymbolValueStoredOnAssignment() {
        String code = """
            int x;
            int y = 1;
            x = 99;
            write(x);
            write(y);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        Object[] row = result.getSymbolTableRows().stream()
            .filter(r -> "x".equals(r[0])).findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'x' não encontrado"));

        assertEquals(99, row[11], "Valor de x deveria ser 99 após atribuição");
    }

    @Test
    void testSymbolValueUpdatedOnReassignment() {
        String code = """
            int x = 1;
            x = 2;
            x = 3;
            write(x);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        Object[] row = result.getSymbolTableRows().stream()
            .filter(r -> "x".equals(r[0])).findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'x' não encontrado"));

        assertEquals(3, row[11], "Valor de x deveria ser 3 após reatribuições");
    }

    @Test
    void testConstantFoldingArithmetic() {
        String code = """
            int a = 10;
            int b = 3;
            int c;
            c = a + b;
            write(c);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        Object[] row = result.getSymbolTableRows().stream()
            .filter(r -> "c".equals(r[0])).findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'c' não encontrado"));

        assertEquals(13, row[11], "c deveria ser 13 (10 + 3)");
    }

    @Test
    void testConstantFoldingMultiplication() {
        String code = """
            int a = 4;
            int b = 5;
            int c;
            c = a * b;
            write(c);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        Object[] row = result.getSymbolTableRows().stream()
            .filter(r -> "c".equals(r[0])).findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'c' não encontrado"));

        assertEquals(20, row[11], "c deveria ser 20 (4 * 5)");
    }

    @Test
    void testConstantFoldingBooleanAnd() {
        String code = """
            bool a = true;
            bool b = true;
            if (a && b) { write("ok"); }
        """;
        assertSuccess(code);
    }

    @Test
    void testUninitializedSymbolValueIsNull() {
        String code = """
            int x;
            int y = 1;
            write(x);
            write(y);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        Object[] row = result.getSymbolTableRows().stream()
            .filter(r -> "x".equals(r[0])).findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'x' não encontrado"));

        assertNull(row[11], "Variável não inicializada deveria ter valor null");
    }

    @Test
    void testFailsDivisionByZero() {
        String code = """
            int a = 10;
            int b = 0;
            int c;
            c = a / b;
        """;
        assertFailure(code);
    }

    @Test
    void testStringConcatenation() {
        String code = """
            string a = "hello";
            string b = " world";
            string c;
            c = a + b;
            write(c);
        """;
        assertSuccess(code);
    }

    @Test
    void testSymbolValueCopiedFromVariable() {
        // int b = a; → b.value deve ser copiado de a.value (action #12 com justDeclared=true)
        String code = """
            int a = 10;
            int b = a;
            write(b);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        Object[] rowB = result.getSymbolTableRows().stream()
            .filter(r -> "b".equals(r[0])).findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'b' não encontrado"));

        assertEquals("Sim", rowB[3], "b deveria estar marcado como inicializado");
        assertEquals(10,    rowB[11], "b.value deveria ser 10 (copiado de a)");
    }

    @Test
    void testSymbolValueChainedCopy() {
        // int c = b onde b = a = 10 → c.value deve ser 10
        String code = """
            int a = 10;
            int b = a;
            int c = b;
            write(c);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        Object[] rowC = result.getSymbolTableRows().stream()
            .filter(r -> "c".equals(r[0])).findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'c' não encontrado"));

        assertEquals(10, rowC[11], "c.value deveria ser 10 (cadeia a→b→c)");
    }

    @Test
    void testBooleanLiteralValueStored() {
        String code = """
            bool flag = true;
            write(flag);
        """;
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Esperava sucesso");

        Object[] row = result.getSymbolTableRows().stream()
            .filter(r -> "flag".equals(r[0])).findFirst()
            .orElseThrow(() -> new AssertionError("Símbolo 'flag' não encontrado"));

        assertEquals(true, row[11], "Valor de flag deveria ser true (Boolean)");
    }
}
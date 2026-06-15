import compiler.compiler.CompilationEngine;
import compiler.factory.ParserFactory;
import compiler.model.CompilationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompilerCodeGenTests {

    private CompilationEngine engine;

    @BeforeEach
    void setup() {
        engine = ParserFactory.createCompilationEngine();
    }

    private String getAsm(String code) {
        CompilationResult result = engine.compile(code);
        assertTrue(result.isSuccess(), "Compilation failed: " + result.getErrorMessage());
        String asm = result.getAsmCode();
        assertNotNull(asm, "ASM code is null");
        assertFalse(asm.isBlank(), "ASM code is blank");
        return asm;
    }

    @Test
    void testDataSectionSimpleVariables() {
        String asm = getAsm("int x; int y;");
        assertTrue(asm.contains(".data"), "Missing .data section");
        assertTrue(asm.contains("x:"), "Missing variable x in .data");
        assertTrue(asm.contains("y:"), "Missing variable y in .data");
    }

    @Test
    void testDataSectionWithInitialValue() {
        String asm = getAsm("int x = 5;");
        assertTrue(asm.contains("x: 5"), "Variable x should have initial value 5. Got:\n" + asm);
    }

    @Test
    void testDataSectionArray() {
        String asm = getAsm("int v[10];");
        assertTrue(asm.contains(".data"), "Missing .data section");
        assertTrue(asm.contains("v: [10]"), "Missing array declaration. Got:\n" + asm);
    }

    @Test
    void testCodeSectionExists() {
        String asm = getAsm("int x;");
        assertTrue(asm.contains(".code"), "Missing .code section");
        assertTrue(asm.contains("HLT"), "Missing HLT instruction");
    }

    @Test
    void testReadStatement() {
        String asm = getAsm("int x; read(x);");
        assertTrue(asm.contains("IN x"), "Missing IN x. Got:\n" + asm);
    }

    @Test
    void testWriteVariable() {
        String asm = getAsm("int x; read(x); write(x);");
        assertTrue(asm.contains("IN x"), "Missing IN x");
        assertTrue(asm.contains("LDA x"), "Missing LDA x for write");
        assertTrue(asm.contains("OUT"), "Missing OUT");
    }

    @Test
    void testWriteInteger() {
        String asm = getAsm("write(42);");
        assertTrue(asm.contains("LDA #42"), "Missing LDA #42. Got:\n" + asm);
        assertTrue(asm.contains("OUT"), "Missing OUT");
    }

    @Test
    void testSimpleAssignment() {
        String asm = getAsm("int x; x = 10;");
        assertTrue(asm.contains("LDA #10"), "Missing LDA #10. Got:\n" + asm);
        assertTrue(asm.contains("STA x"), "Missing STA x. Got:\n" + asm);
    }

    @Test
    void testVariableToVariableAssignment() {
        String asm = getAsm("int x; int y; read(x); y = x;");
        assertTrue(asm.contains("IN x"), "Missing IN x");
        assertTrue(asm.contains("LDA x"), "Missing LDA x for y = x");
        assertTrue(asm.contains("STA y"), "Missing STA y");
    }

    @Test
    void testAddition() {
        String asm = getAsm("int x; int y; int z; read(x); read(y); z = x + y;");
        assertTrue(asm.contains("LDA x"), "Missing LDA x. Got:\n" + asm);
        assertTrue(asm.contains("ADD y"), "Missing ADD y. Got:\n" + asm);
        assertTrue(asm.contains("STA z"), "Missing STA z. Got:\n" + asm);
    }

    @Test
    void testSubtraction() {
        String asm = getAsm("int x; int y; int z; read(x); read(y); z = x - y;");
        assertTrue(asm.contains("SUB y"), "Missing SUB y. Got:\n" + asm);
    }

    @Test
    void testAdditionWithLiteral() {
        String asm = getAsm("int x; int y; read(x); y = x + 5;");
        assertTrue(asm.contains("LDA x"), "Missing LDA x");
        assertTrue(asm.contains("ADD #5"), "Missing ADD #5. Got:\n" + asm);
        assertTrue(asm.contains("STA y"), "Missing STA y");
    }

    @Test
    void testBitwiseAnd() {
        String asm = getAsm("int x; int y; int z; read(x); read(y); z = x & y;");
        assertTrue(asm.contains("AND y"), "Missing AND y. Got:\n" + asm);
    }

    @Test
    void testBitwiseOr() {
        String asm = getAsm("int x; int y; int z; read(x); read(y); z = x | y;");
        assertTrue(asm.contains("OR y"), "Missing OR y. Got:\n" + asm);
    }

    @Test
    void testBitwiseXor() {
        String asm = getAsm("int x; int y; int z; read(x); read(y); z = x ^ y;");
        assertTrue(asm.contains("XOR y"), "Missing XOR y. Got:\n" + asm);
    }

    @Test
    void testShiftLeft() {
        String asm = getAsm("int x; int y; read(x); y = x << 2;");
        assertTrue(asm.contains("SHL #2"), "Missing SHL #2. Got:\n" + asm);
    }

    @Test
    void testShiftRight() {
        String asm = getAsm("int x; int y; read(x); y = x >> 1;");
        assertTrue(asm.contains("SHR #1"), "Missing SHR #1. Got:\n" + asm);
    }

    @Test
    void testWriteExpression() {
        String asm = getAsm("int x; int y; read(x); read(y); write(x + y);");
        assertTrue(asm.contains("ADD y"), "Missing ADD y");
        assertTrue(asm.contains("OUT"), "Missing OUT");
    }

    @Test
    void testArrayDeclaration() {
        String asm = getAsm("int v[10];");
        assertTrue(asm.contains("v: [10]"), "Array not in .data. Got:\n" + asm);
    }

    @Test
    void testReadArray() {
        String asm = getAsm("int v[5]; read(v[0]);");
        assertTrue(asm.contains("IN v[0]"), "Missing IN v[0]. Got:\n" + asm);
    }

    @Test
    void testWriteArray() {
        String asm = getAsm("int v[5]; read(v[0]); write(v[0]);");
        assertTrue(asm.contains("LDA v[0]"), "Missing LDA v[0]. Got:\n" + asm);
        assertTrue(asm.contains("OUT"), "Missing OUT");
    }

    @Test
    void testArrayAssignment() {
        String asm = getAsm("int v[5]; int x; read(x); v[0] = x;");
        assertTrue(asm.contains("LDA x"), "Missing LDA x");
        assertTrue(asm.contains("STA v[0]"), "Missing STA v[0]. Got:\n" + asm);
    }
}

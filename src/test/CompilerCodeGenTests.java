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
        assertTrue(asm.contains(".text"), "Missing .text section");
        assertTrue(asm.contains("HLT"), "Missing HLT instruction");
    }

    @Test
    void testReadStatement() {
        String asm = getAsm("int x; read(x);");
        assertTrue(asm.contains("LD $in_port"), "Missing LD $in_port. Got:\n" + asm);
        assertTrue(asm.contains("STO x"), "Missing STO x. Got:\n" + asm);
    }

    @Test
    void testWriteVariable() {
        String asm = getAsm("int x; read(x); write(x);");
        assertTrue(asm.contains("LD $in_port"), "Missing LD $in_port");
        assertTrue(asm.contains("LD x"), "Missing LD x for write");
        assertTrue(asm.contains("STO $out_port"), "Missing STO $out_port");
    }

    @Test
    void testWriteInteger() {
        String asm = getAsm("write(42);");
        assertTrue(asm.contains("LDI 42"), "Missing LDI 42. Got:\n" + asm);
        assertTrue(asm.contains("STO $out_port"), "Missing STO $out_port");
    }

    @Test
    void testSimpleAssignment() {
        String asm = getAsm("int x; x = 10;");
        assertTrue(asm.contains("LDI 10"), "Missing LDI 10. Got:\n" + asm);
        assertTrue(asm.contains("STO x"), "Missing STO x. Got:\n" + asm);
    }

    @Test
    void testVariableToVariableAssignment() {
        String asm = getAsm("int x; int y; read(x); y = x;");
        assertTrue(asm.contains("LD $in_port"), "Missing LD $in_port");
        assertTrue(asm.contains("LD x"), "Missing LD x for y = x");
        assertTrue(asm.contains("STO y"), "Missing STO y");
    }

    @Test
    void testAddition() {
        String asm = getAsm("int x; int y; int z; read(x); read(y); z = x + y;");
        assertTrue(asm.contains("LD x"), "Missing LD x. Got:\n" + asm);
        assertTrue(asm.contains("ADD y"), "Missing ADD y. Got:\n" + asm);
        assertTrue(asm.contains("STO z"), "Missing STO z. Got:\n" + asm);
    }

    @Test
    void testSubtraction() {
        String asm = getAsm("int x; int y; int z; read(x); read(y); z = x - y;");
        assertTrue(asm.contains("SUB y"), "Missing SUB y. Got:\n" + asm);
    }

    @Test
    void testAdditionWithLiteral() {
        String asm = getAsm("int x; int y; read(x); y = x + 5;");
        assertTrue(asm.contains("LD x"), "Missing LD x");
        assertTrue(asm.contains("ADDI 5"), "Missing ADDI 5. Got:\n" + asm);
        assertTrue(asm.contains("STO y"), "Missing STO y");
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
        assertTrue(asm.contains("SLL"), "Missing SLL. Got:\n" + asm);
    }

    @Test
    void testShiftRight() {
        String asm = getAsm("int x; int y; read(x); y = x >> 1;");
        assertTrue(asm.contains("SRL"), "Missing SRL. Got:\n" + asm);
    }

    @Test
    void testWriteExpression() {
        String asm = getAsm("int x; int y; read(x); read(y); write(x + y);");
        assertTrue(asm.contains("ADD y"), "Missing ADD y");
        assertTrue(asm.contains("STO $out_port"), "Missing STO $out_port");
    }

    @Test
    void testArrayDeclaration() {
        String asm = getAsm("int v[10];");
        assertTrue(asm.contains("v: [10]"), "Array not in .data. Got:\n" + asm);
    }

    @Test
    void testReadArray() {
        String asm = getAsm("int v[5]; read(v[0]);");
        assertTrue(asm.contains("LD $in_port"), "Missing LD $in_port. Got:\n" + asm);
        assertTrue(asm.contains("STOV v"), "Missing STOV v. Got:\n" + asm);
    }

    @Test
    void testWriteArray() {
        String asm = getAsm("int v[5]; read(v[0]); write(v[0]);");
        assertTrue(asm.contains("LDV v"), "Missing LDV v. Got:\n" + asm);
        assertTrue(asm.contains("STO $out_port"), "Missing STO $out_port");
    }

    @Test
    void testArrayAssignment() {
        String asm = getAsm("int v[5]; int x; read(x); v[0] = x;");
        assertTrue(asm.contains("LD x"), "Missing LD x");
        assertTrue(asm.contains("STOV v"), "Missing STOV v. Got:\n" + asm);
    }
}

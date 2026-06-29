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
        String asm = getAsm("int v[3];");
        assertTrue(asm.contains(".data"), "Missing .data section");
        assertTrue(asm.contains("v: 0,0,0"), "Missing array declaration. Got:\n" + asm);
    }

    void testDataSectionArrayWithInitialValue() {
        String asm = getAsm("int v[3] = {1, 2, 3};");
        assertTrue(asm.contains(".data"), "Missing .data section");
        assertTrue(asm.contains("v: 1,2,3"), "Missing array declaration. Got:\n" + asm);
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
        String asm = getAsm("int v[3];");
        assertTrue(asm.contains("v: 0,0,0"), "Array not in .data. Got:\n" + asm);
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

    @Test
    void testComplexCode() {
        String asm = getAsm("int v[5]; int x; read(x); v[0] = x;");
        assertTrue(asm.contains("LD x"), "Missing LD x");
        assertTrue(asm.contains("STOV v"), "Missing STOV v. Got:\n" + asm);
    }

    @Test
    void testRelationalOperation() {
        String asm = getAsm("""
            int a = 2;
            if (a == 2) {
                write(a);
            }
        """);

        assertTrue(asm.contains("SUB"), "Missing SUB comparison.\n" + asm);
        assertTrue(asm.contains("BNE") || asm.contains("BEQ"),
                "Missing conditional branch.\n" + asm);
    }

    @Test
    void testIf() {
        String asm = getAsm("""
        int v1 = 2;
        int v2 = 5;
        if(v1 > v2){
            v2 = v1;
        }
        write(v1);
        write(v2);
    """);

        assertTrue(asm.contains("LDI 2"), "Missing initialization of v1.\n" + asm);
        assertTrue(asm.contains("STO v1"), "Missing store of v1.\n" + asm);
        assertTrue(asm.contains("LDI 5"), "Missing initialization of v2.\n" + asm);
        assertTrue(asm.contains("STO v2"), "Missing store of v2.\n" + asm);
        assertTrue(asm.contains("LD v1"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("STO 1000"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("LD v2"), "Missing load of v2.\n" + asm);
        assertTrue(asm.contains("STO 1001"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("LD 1000"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("SUB 1001"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("BLE FIMSE1"), "Missing BLE instruction.\n" + asm);
        assertTrue(asm.contains("LD v1"), "Missing assignment v2 = v1.\n" + asm);
        assertTrue(asm.contains("STO v2"), "Missing assignment v2 = v1.\n" + asm);
        assertTrue(asm.contains("FIMSE1:"), "Missing FIMSE label.\n" + asm);
        assertTrue(asm.contains("LD v1"), "Missing assignment v2 = v1.\n" + asm);
        assertTrue(asm.contains("STO $out_port"), "Missing output instruction.\n" + asm);
        assertTrue(asm.contains("LD v2"), "Missing assignment v2 = v1.\n" + asm);
        assertTrue(asm.contains("STO $out_port"), "Missing output instruction.\n" + asm);
        assertTrue(asm.contains("HLT 0"), "Missing HLT instruction.\n" + asm);
    }

    @Test
    void testIfElse() {
        String asm = getAsm("""
        int v1 = 2;
        int v2 = 5;
        if(v1 > v2){
            v2 = v1;
        } else {
            v1 = 0;
        }
        write(v1);
        write(v2);
    """);

        assertTrue(asm.contains("LDI 2"), "Missing initialization of v1.\n" + asm);
        assertTrue(asm.contains("STO v1"), "Missing store of v1.\n" + asm);
        assertTrue(asm.contains("LDI 5"), "Missing initialization of v2.\n" + asm);
        assertTrue(asm.contains("STO v2"), "Missing store of v2.\n" + asm);
        assertTrue(asm.contains("LD v1"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("STO 1000"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("LD v2"), "Missing load of v2.\n" + asm);
        assertTrue(asm.contains("STO 1001"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("LD 1000"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("SUB 1001"), "Missing load of v1.\n" + asm);
        assertTrue(asm.contains("BLE FIMSE1"), "Missing BLE instruction.\n" + asm);
        assertTrue(asm.contains("LD v1"), "Missing assignment v2 = v1.\n" + asm);
        assertTrue(asm.contains("STO v2"), "Missing assignment v2 = v1.\n" + asm);
        assertTrue(asm.contains("JMP ELSE2"), "Missing assignment v2 = v1.\n" + asm);
        assertTrue(asm.contains("FIMSE1:"), "Missing FIMSE label.\n" + asm);
        assertTrue(asm.contains("LDI 0"), "Missing FIMSE label.\n" + asm);
        assertTrue(asm.contains("STO v1"), "Missing FIMSE label.\n" + asm);
        assertTrue(asm.contains("ELSE2:"), "Missing FIMSE label.\n" + asm);
        assertTrue(asm.contains("LD v1"), "Missing assignment v2 = v1.\n" + asm);
        assertTrue(asm.contains("STO $out_port"), "Missing output instruction.\n" + asm);
        assertTrue(asm.contains("LD v2"), "Missing assignment v2 = v1.\n" + asm);
        assertTrue(asm.contains("STO $out_port"), "Missing output instruction.\n" + asm);
        assertTrue(asm.contains("HLT 0"), "Missing HLT instruction.\n" + asm);
    }

    @Test
    void testIfElseStatement() {
        String asm = getAsm("""
            int a = 2;
            if (a == 2) {
                write(a);
            } else {
                read(a);
            }
        """);

        assertTrue(asm.contains("LDI 2"));
        assertTrue(asm.contains("STO a"));
        assertTrue(asm.contains("LD a"));
        assertTrue(asm.contains("STO 1000"));
        assertTrue(asm.contains("LDI 2"));
        assertTrue(asm.contains("STO 1001"));
        assertTrue(asm.contains("LD 1000"));
        assertTrue(asm.contains("SUB 1001"));
        assertTrue(asm.contains("BNE FIMSE1"));
        assertTrue(asm.contains("LD a"));
        assertTrue(asm.contains("STO $out_port"));
        assertTrue(asm.contains("JMP ELSE2"));
        assertTrue(asm.contains("ELSE2:"));
        assertTrue(asm.contains("LD $in_port"));
        assertTrue(asm.contains("STO a"));
        assertTrue(asm.contains("FIMSE1:"));
        assertTrue(asm.contains("HLT 0"));
    }

    @Test
    void testWhileLoop() {
        String asm = getAsm("""
        int i = 0;
        while (i < 10) {
            i = i + 1;
        }
        write(i);
    """);

        assertTrue(asm.contains("BEGIN_WHILE1:"));
        assertTrue(asm.contains("LD i"));
        assertTrue(asm.contains("STO 1000"));
        assertTrue(asm.contains("LDI 10"));
        assertTrue(asm.contains("STO 1001"));
        assertTrue(asm.contains("LD 1000"));
        assertTrue(asm.contains("SUB 1001"));
        assertTrue(asm.contains("BGE END_WHILE2"));
        assertTrue(asm.contains("LD i"));
        assertTrue(asm.contains("ADDI 1"));
        assertTrue(asm.contains("STO i"));
        assertTrue(asm.contains("JMP BEGIN_WHILE1"));
        assertTrue(asm.contains("END_WHILE2:"));
        assertTrue(asm.contains("LD i"));
        assertTrue(asm.contains("STO $out_port"));
        assertTrue(asm.contains("HLT 0"));
    }

    @Test
    void testDoWhileLoop() {
        String asm = getAsm("""
            int cont = 0;
            do {
                cont = cont + 1;
            } while (cont <= 5);
            write(cont);
        """);

        assertTrue(asm.contains("DO_WHILE1:"));
        assertTrue(asm.contains("LD cont"));
        assertTrue(asm.contains("ADDI 1"));
        assertTrue(asm.contains("STO cont"));
        assertTrue(asm.contains("LD cont"));
        assertTrue(asm.contains("STO 1000"));
        assertTrue(asm.contains("LD 1000"));
        assertTrue(asm.contains("SUB 1001"));
        assertTrue(asm.contains("BLE DO_WHILE1"));
        assertTrue(asm.contains("LD cont"));
        assertTrue(asm.contains("STO $out_port"));
        assertTrue(asm.contains("HLT 0"));
    }

    @Test
    void testForLoop() {
        String asm = getAsm("""
            for(int i = 1; i < 10; i = i + 1){
                write(i);
            }
        """);

        assertTrue(asm.contains("LDI 1"));
        assertTrue(asm.contains("STO i"));
        assertTrue(asm.contains("BEGIN_FOR1:"));
        assertTrue(asm.contains("LD i"));
        assertTrue(asm.contains("STO 1000"));
        assertTrue(asm.contains("LDI 10"));
        assertTrue(asm.contains("STO 1001"));
        assertTrue(asm.contains("LD 1000"));
        assertTrue(asm.contains("SUB 1001"));
        assertTrue(asm.contains("BGE END_FOR2"));
        assertTrue(asm.contains("LD i"));
        assertTrue(asm.contains("STO $out_port"));
        assertTrue(asm.contains("LD i"));
        assertTrue(asm.contains("ADDI 1"));
        assertTrue(asm.contains("STO i"));
        assertTrue(asm.contains("JMP BEGIN_FOR1"));
        assertTrue(asm.contains("END_FOR2:"));
        assertTrue(asm.contains("HLT 0"));
    }

    @Test
    void testNestedStructures() {
        String asm = getAsm("""
        int i = 0;
        int j = 0;

        while(i < 10){
            if(j == 0){
                j = j + 1;
            } else {
                j = j - 1;
            }
            i = i + 1;
        }

        write(i);
        write(j);
    """);

        // Inicialização
        assertTrue(asm.contains("LDI 0"));
        assertTrue(asm.contains("STO i"));
        assertTrue(asm.contains("STO j"));

        // Início do while
        assertTrue(asm.contains("BEGIN_WHILE1:"));

        // Comparação do while
        assertTrue(asm.contains("LD i"));
        assertTrue(asm.contains("STO 1000"));
        assertTrue(asm.contains("LDI 10"));
        assertTrue(asm.contains("STO 1001"));
        assertTrue(asm.contains("LD 1000"));
        assertTrue(asm.contains("SUB 1001"));
        assertTrue(asm.contains("BGE END_WHILE2"));

        // Comparação do if
        assertTrue(asm.contains("LD j"));
        assertTrue(asm.contains("LDI 0"));
        assertTrue(asm.contains("BNE FIMSE3"));

        // Corpo do if
        assertTrue(asm.contains("ADDI 1"));
        assertTrue(asm.contains("STO j"));

        // Fim do if
        assertTrue(asm.contains("FIMSE3:"));

        // Incremento do while
        assertTrue(asm.contains("LD i"));
        assertTrue(asm.contains("ADDI 1"));
        assertTrue(asm.contains("STO i"));

        // Retorno do laço
        assertTrue(asm.contains("JMP BEGIN_WHILE1"));

        // Fim do while
        assertTrue(asm.contains("END_WHILE2:"));

        // Escrita das variáveis
        assertTrue(asm.contains("LD i"));
        assertTrue(asm.contains("STO $out_port"));
        assertTrue(asm.contains("LD j"));
        assertTrue(asm.contains("STO $out_port"));

        // Finalização
        assertTrue(asm.contains("HLT 0"));
    }

//
//    @Test
//    void testFunctionReturn() {
//        String asm = getAsm("""
//            int soma(){
//                return 10;
//            }
//
//            int x;
//            x = soma();
//        """);
//
//        assertTrue(asm.contains("RET"),
//                "Missing RET instruction.\n" + asm);
//
//        assertTrue(asm.contains("CALL"));
//    }
//
//    @Test
//    void testReturnInsideExpression() {
//        String asm = getAsm("""
//            int f(){
//                return 2;
//            }
//
//            int x;
//            x = f() + 3;
//        """);
//
//        assertTrue(asm.contains("CALL"));
//        assertTrue(asm.contains("ADD"));
//    }
//
//    @Test
//    void testWrongParameterCount() {
//        Exception ex = assertThrows(RuntimeException.class, () ->
//                getAsm("""
//            void soma(int a, int b){}
//            soma(1);
//        """)
//        );
//
//        assertTrue(ex.getMessage().contains("esperava 2 parâmetros"));
//    }
//
////    @Test
////    void testWrongParameterType() {
////        Exception ex = assertThrows(RuntimeException.class, () ->
////                getAsm("""
////            void teste(int a){}
////            teste(true);
////        """)
////        );
////
////        assertTrue(ex.getMessage().contains("tipo"));
////    }
//
//    @Test
//    void testWrongParameterOrder() {
//        Exception ex = assertThrows(RuntimeException.class, () ->
//                getAsm("""
//            void teste(int a, boolean b){}
//            teste(false, 1);
//        """)
//        );
//
//        assertTrue(ex.getMessage().contains("parâmetro"));
//    }
//
//    @Test
//    void testUndefinedFunction() {
//        Exception ex = assertThrows(RuntimeException.class, () ->
//                getAsm("""
//            teste();
//        """)
//        );
//
//        assertTrue(ex.getMessage().contains("não existe"));
//    }
//
//    @Test
//    void testMeaningfulErrorMessage() {
//        Exception ex = assertThrows(RuntimeException.class, () ->
//                getAsm("""
//            foo();
//        """)
//        );
//
//        assertFalse(ex.getMessage().isBlank());
//    }

    //    @Test
//    void testCall() {
//        String asm = getAsm("""
//        void teste(){
//            write(1);
//        }
//
//        teste();
//    """);
//
//        assertTrue(asm.contains("CALL"));
//        assertTrue(asm.contains("RETURN"));
//    }
//
////    @Test
////    void testReturn() {
////        String asm = getAsm("""
////        int teste(){
////            return 5;
////        }
////
////        int a;
////
////        a = teste();
////    """);
////
////        assertTrue(asm.contains("CALL"));
////        assertTrue(asm.contains("RETURN"));
////        assertTrue(asm.contains("STO     a"));
////    }
////
////    @Test
////    void testParameterPassing() {
////        String asm = getAsm("""
////        void soma(int a, int b){
////            write(a);
////        }
////
////        soma(1,2);
////    """);
////
////        assertTrue(asm.contains("CALL"));
////
////        long stores = asm.lines()
////                .filter(s -> s.contains("STO"))
////                .count();
////
////        assertTrue(stores >= 2);
////    }
////
////    @Test
////    void testWrongNumberOfParameters() {
////
////        Exception ex = assertThrows(RuntimeException.class,
////                () -> getAsm("""
////            void soma(int a, int b){}
////
////            soma(1);
////        """));
////
////        assertTrue(ex.getMessage().contains("esperava 2 parâmetros"));
////    }
////
////    @Test
////    void testUnknownProcedure() {
////
////        Exception ex = assertThrows(RuntimeException.class,
////                () -> getAsm("""
////            teste();
////        """));
////
////        assertTrue(ex.getMessage().contains("não existe"));
////    }
}

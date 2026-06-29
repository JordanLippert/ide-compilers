package compiler.codegen;

import compiler.gals.Symbol;
import compiler.gals.Token;

import java.util.List;

public interface ICodeGenerator {
    void generateVariable(Symbol symbol);
    void generateCode(List<Token> tokens);
    String getAssemblyCode();
}

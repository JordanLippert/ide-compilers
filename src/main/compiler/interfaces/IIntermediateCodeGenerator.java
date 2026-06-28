package compiler.interfaces;

import compiler.models.IntermediateCodeGeneratorResult;
import compiler.models.Symbol;
import compiler.models.Token;

import java.util.List;

public interface IIntermediateCodeGenerator {
    public IntermediateCodeGeneratorResult generate(List<Token> tokens, List<Symbol> symbolTable);
}

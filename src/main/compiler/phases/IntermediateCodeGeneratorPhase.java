package compiler.phases;

import compiler.interfaces.IIntermediateCodeGenerator;
import compiler.models.*;

import java.util.ArrayList;
import java.util.List;

public class IntermediateCodeGeneratorPhase {
    IIntermediateCodeGenerator _intermediateCodeGenerator;

    public IntermediateCodeGeneratorPhase(IIntermediateCodeGenerator intermediateCodeGenerator) {
        _intermediateCodeGenerator = intermediateCodeGenerator;
    }

    public void execute(List<Token> tokens, List<Symbol> symbolsTable) {
        IntermediateCodeGeneratorResult result = _intermediateCodeGenerator.generate(tokens, symbolsTable);

        // Transform each lexical error do a compilation error
        List<CompilationError> compilationErrors = new ArrayList<CompilationError>();
        List<CodeGenerationError> lexicalErrors = result.getErrors();
        for (CodeGenerationError lexicalError : lexicalErrors) {
            CompilationError error = CompilationError.FromError(lexicalError);
            compilationErrors.add(error);
        }
    }

    public String getPhaseName() {
        return "Lexical Analysis";
    }
}

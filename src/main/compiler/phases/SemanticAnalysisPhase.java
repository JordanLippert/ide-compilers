package compiler.phases;

import compiler.interfaces.ISemanticAnalyzer;
import compiler.models.CompilationError;
import compiler.models.SemanticAnalysisResult;
import compiler.models.SemanticError;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalysisPhase{
    ISemanticAnalyzer _semanticAnalyzer;

    public SemanticAnalysisPhase(ISemanticAnalyzer SemanticAnalyzer) {
        _semanticAnalyzer = SemanticAnalyzer;
    }

    public void execute(String sourceCode) {
        SemanticAnalysisResult result = _semanticAnalyzer.analyzeSemantic(sourceCode);

        // Transform each synthatic error do a compilation error
        List<CompilationError> compilationErrors = new ArrayList<CompilationError>();
        List<SemanticError> SemanticErrors = result.getErrors();
        for (SemanticError SemanticError : SemanticErrors) {
            CompilationError error = CompilationError.FromError(SemanticError);
            compilationErrors.add(error);
        }
    }

    public String getPhaseName() {
        return "Lexical Analysis";
    }
}

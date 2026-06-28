package compiler.phases;

import compiler.interfaces.ISyntaxAnalyzer;
import compiler.models.CompilationError;
import compiler.models.SyntacticError;
import compiler.models.SyntaxAnalysisResult;

import java.util.ArrayList;
import java.util.List;

public class SyntaxAnalysisPhase {
    ISyntaxAnalyzer _syntaxAnalyzer;

    public SyntaxAnalysisPhase(ISyntaxAnalyzer SyntaxAnalyzer) {
        _syntaxAnalyzer = SyntaxAnalyzer;
    }

    public void execute(String sourceCode) {
        SyntaxAnalysisResult result = _syntaxAnalyzer.analyzeSyntax(sourceCode);

        // Transform each synthatic error do a compilation error
        List<CompilationError> compilationErrors = new ArrayList<CompilationError>();
        List<SyntacticError> SyntaxErrors = result.getErrors();
        for (SyntacticError SyntaxError : SyntaxErrors) {
            CompilationError error = CompilationError.FromError(SyntaxError);
            compilationErrors.add(error);
        }
    }

    public String getPhaseName() {
        return "Syntax Analysis";
    }
}

package compiler.phases;

import compiler.interfaces.ICompilationPhase;
import compiler.interfaces.ILexicalAnalyzer;
import compiler.models.CompilationContext;
import compiler.models.CompilationError;
import compiler.models.LexicalAnalysisResult;
import compiler.models.LexicalError;

import java.util.ArrayList;
import java.util.List;

public class LexicalAnalysisPhase implements ICompilationPhase {
    ILexicalAnalyzer _lexicalAnalyzer;

    public LexicalAnalysisPhase(ILexicalAnalyzer lexicalAnalyzer) {
        _lexicalAnalyzer = lexicalAnalyzer;
    }

    public void execute(CompilationContext context) {
        String sourceCode = context.getSourceCode();
        LexicalAnalysisResult result = _lexicalAnalyzer.analyzeLexical(context.getSourceCode());

        // Transform each lexical error do a compilation error
        List<CompilationError> compilationErrors = new ArrayList<CompilationError>();
        List<LexicalError> lexicalErrors = result.getErrors();
        for (LexicalError lexicalError : lexicalErrors) {
            CompilationError error = CompilationError.FromError(lexicalError);
            compilationErrors.add(error);
        }
    }

    public String getPhaseName() {
        return "Lexical Analysis";
    }
}

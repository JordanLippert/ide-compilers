package compiler.compiler;

import compiler.adapter.IGalsAdapter;
import compiler.gals.LexicalError;
import compiler.gals.SemanticError;
import compiler.gals.SyntacticError;
import compiler.model.CompilationResult;

/**
 * Fase de análise léxica
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class LexicalPhase implements ICompilationPhase {
    
    private final IGalsAdapter adapter;
    
    public LexicalPhase(IGalsAdapter adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public CompilationResult execute(String sourceCode) throws LexicalError, SyntacticError, SemanticError {
        return adapter.performLexicalAnalysis(sourceCode);
    }
    
    @Override
    public String getPhaseName() {
        return "Análise Léxica";
    }
}

package br.compiler.compiler;

import br.compiler.adapter.IGalsAdapter;
import br.compiler.gals.LexicalError;
import br.compiler.gals.SemanticError;
import br.compiler.gals.SyntacticError;
import br.compiler.model.CompilationResult;

/**
 * Fase de análise sintática
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class SyntacticPhase implements ICompilationPhase {
    
    private final IGalsAdapter adapter;
    
    public SyntacticPhase(IGalsAdapter adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public CompilationResult execute(String sourceCode) throws LexicalError, SyntacticError, SemanticError {
        return adapter.performSyntacticAnalysis(sourceCode);
    }
    
    @Override
    public String getPhaseName() {
        return "Análise Sintática";
    }
}

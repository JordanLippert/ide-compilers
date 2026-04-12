package br.compiler.compiler;

import br.compiler.adapter.IGalsAdapter;
import br.compiler.model.CompilationResult;

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
    public CompilationResult execute(String sourceCode) {
        return adapter.performLexicalAnalysis(sourceCode);
    }
    
    @Override
    public String getPhaseName() {
        return "Análise Léxica";
    }
}

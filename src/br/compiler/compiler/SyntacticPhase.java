package br.compiler.compiler;

import br.compiler.adapter.GalsAdapter;
import br.compiler.model.CompilationResult;

/**
 * Fase de análise sintática
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class SyntacticPhase implements CompilationPhase {
    
    private final GalsAdapter adapter;
    
    public SyntacticPhase(GalsAdapter adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public CompilationResult execute(String sourceCode) {
        return adapter.performSyntacticAnalysis(sourceCode);
    }
    
    @Override
    public String getPhaseName() {
        return "Análise Sintática";
    }
}

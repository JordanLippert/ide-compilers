package br.compiler.compiler;

import br.compiler.adapter.GalsAdapter;
import br.compiler.model.CompilationResult;

/**
 * Fase de análise semântica (futura implementação)
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class SemanticPhase implements CompilationPhase {
    
    private final GalsAdapter adapter;
    
    public SemanticPhase(GalsAdapter adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public CompilationResult execute(String sourceCode) {
        // TODO: Implementar análise semântica futuramente
        return CompilationResult.success();
    }
    
    @Override
    public String getPhaseName() {
        return "Análise Semântica";
    }
}

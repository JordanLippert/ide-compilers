package compiler.compiler;

import compiler.adapter.IGalsAdapter;
import compiler.gals.LexicalError;
import compiler.gals.SemanticError;
import compiler.gals.Semantico;
import compiler.gals.SyntacticError;
import compiler.model.CompilationResult;

/**
 * Fase de análise semântica (futura implementação)
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class SemanticPhase implements ICompilationPhase {
    
    private final IGalsAdapter adapter;
    
    public SemanticPhase(IGalsAdapter adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public CompilationResult execute(String sourceCode) throws LexicalError, SyntacticError, SemanticError {
        return adapter.performSyntacticAnalysis(sourceCode);
    }
    
    @Override
    public String getPhaseName() {
        return "Análise Semântica";
    }
}

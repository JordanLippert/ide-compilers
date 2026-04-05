package br.compiler.adapter;

import br.compiler.model.CompilationResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação temporária do GalsAdapter
 * Será substituída quando as classes GALS forem geradas
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class GalsParserAdapter implements GalsAdapter {
    
    private List<Object> tokens = new ArrayList<>();
    
    @Override
    public CompilationResult performLexicalAnalysis(String sourceCode) {
        // TODO: Implementar quando classes GALS forem geradas
        // Por enquanto, apenas simula sucesso
        return CompilationResult.success();
    }
    
    @Override
    public CompilationResult performSyntacticAnalysis(String sourceCode) {
        // TODO: Implementar quando classes GALS forem geradas
        // Por enquanto, apenas simula sucesso
        return CompilationResult.success();
    }
    
    @Override
    public List<Object> getTokens() {
        return new ArrayList<>(tokens);
    }
}

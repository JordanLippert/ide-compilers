package br.compiler.compiler;

import br.compiler.model.CompilationResult;

/**
 * Interface Strategy para as fases de compilação
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public interface CompilationPhase {
    
    /**
     * Executa uma fase da compilação
     * @param sourceCode código fonte
     * @return resultado da compilação
     */
    CompilationResult execute(String sourceCode);
    
    /**
     * Retorna o nome da fase
     * @return nome da fase
     */
    String getPhaseName();
}

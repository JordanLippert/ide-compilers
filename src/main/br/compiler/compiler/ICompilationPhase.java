package br.compiler.compiler;

import br.compiler.gals.LexicalError;
import br.compiler.gals.SemanticError;
import br.compiler.gals.SyntacticError;
import br.compiler.model.CompilationResult;

/**
 * Interface Strategy para as fases de compilação
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public interface ICompilationPhase {
    
    /**
     * Executa uma fase da compilação
     * @param sourceCode código fonte
     * @return resultado da compilação
     */
    CompilationResult execute(String sourceCode) throws LexicalError, SyntacticError, SemanticError;
    
    /**
     * Retorna o nome da fase
     * @return nome da fase
     */
    String getPhaseName();
}

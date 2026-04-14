package compiler.compiler;

import compiler.gals.LexicalError;
import compiler.gals.SemanticError;
import compiler.gals.SyntacticError;
import compiler.model.CompilationResult;

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

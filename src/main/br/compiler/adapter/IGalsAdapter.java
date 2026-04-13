package br.compiler.adapter;

import br.compiler.gals.LexicalError;
import br.compiler.gals.SemanticError;
import br.compiler.gals.SyntacticError;
import br.compiler.model.CompilationResult;
import java.util.List;

/**
 * Interface do Adapter para isolar as classes GALS
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public interface IGalsAdapter {
    
    /**
     * Realiza análise léxica do código fonte
     * @param sourceCode código fonte a ser analisado
     * @return resultado da compilação
     */
    CompilationResult performLexicalAnalysis(String sourceCode) throws LexicalError, SyntacticError, SemanticError;
    
    /**
     * Realiza análise sintática do código fonte
     * @param sourceCode código fonte a ser analisado
     * @return resultado da compilação
     */
    CompilationResult performSyntacticAnalysis(String sourceCode) throws LexicalError, SyntacticError, SemanticError;
    
    /**
     * Obtém os tokens gerados pela análise léxica
     * @return lista de tokens
     */
    List<Object> getTokens();
}

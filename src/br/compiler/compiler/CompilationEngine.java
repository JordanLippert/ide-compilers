package br.compiler.compiler;

import br.compiler.error.*;
import br.compiler.model.CompilationResult;
import br.compiler.model.ErrorMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Motor de compilação que coordena as fases de análise
 * Implementa o padrão Strategy
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class CompilationEngine {
    
    private final List<CompilationPhase> phases = new ArrayList<>();
    private final ErrorHandler errorHandler;
    
    public CompilationEngine() {
        this.errorHandler = setupErrorHandler();
    }
    
    private ErrorHandler setupErrorHandler() {
        ErrorHandler lexical = new LexicalErrorHandler();
        ErrorHandler syntactic = new SyntacticErrorHandler();
        ErrorHandler semantic = new SemanticErrorHandler();
        ErrorHandler formatter = new ErrorFormatter();
        
        lexical.setNext(syntactic);
        syntactic.setNext(semantic);
        semantic.setNext(formatter);
        
        return lexical;
    }
    
    public void addPhase(CompilationPhase phase) {
        phases.add(phase);
    }
    
    public CompilationResult compile(String sourceCode) {
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            return CompilationResult.error("VALIDATION", "Código fonte vazio", 0);
        }
        
        for (CompilationPhase phase : phases) {
            try {
                CompilationResult result = phase.execute(sourceCode);
                
                if (!result.isSuccess()) {
                    ErrorMessage error = errorHandler.handle(result);
                    return CompilationResult.error(error);
                }
                
            } catch (Exception e) {
                return CompilationResult.exception(e);
            }
        }
        
        return CompilationResult.success();
    }
    
    public List<CompilationPhase> getPhases() {
        return new ArrayList<>(phases);
    }
}

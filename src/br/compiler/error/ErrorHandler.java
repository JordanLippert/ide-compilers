package br.compiler.error;

import br.compiler.model.CompilationResult;
import br.compiler.model.ErrorMessage;

/**
 * Classe base do Chain of Responsibility para tratamento de erros
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public abstract class ErrorHandler {
    protected ErrorHandler next;
    
    public void setNext(ErrorHandler handler) {
        this.next = handler;
    }
    
    public ErrorMessage handle(CompilationResult result) {
        ErrorMessage message = processError(result);
        
        if (message != null) {
            if (next != null) {
                return next.handle(result);
            }
            return message;
        }
        
        if (next != null) {
            return next.handle(result);
        }
        
        return null;
    }
    
    protected abstract ErrorMessage processError(CompilationResult result);
}

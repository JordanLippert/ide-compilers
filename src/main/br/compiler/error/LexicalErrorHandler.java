package br.compiler.error;

import br.compiler.model.CompilationResult;
import br.compiler.model.ErrorMessage;
import br.compiler.model.ErrorSeverity;

public class LexicalErrorHandler extends ErrorHandler {
    
    @Override
    protected ErrorMessage processError(CompilationResult result) {
        if (result.isSuccess()) {
            return null;
        }
        
        String errorType = result.getErrorType();
        if (errorType == null || !errorType.toLowerCase().contains("léxico") 
            && !errorType.toLowerCase().contains("lexico")
            && !errorType.toLowerCase().contains("lexical")) {
            return null;
        }
        
        return ErrorMessage.builder()
            .type("LÉXICO")
            .line(result.getErrorLine())
            .position(result.getErrorPosition())
            .message(result.getErrorMessage())
            .severity(ErrorSeverity.ERROR)
            .build();
    }
}

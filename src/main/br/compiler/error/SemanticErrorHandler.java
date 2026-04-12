package br.compiler.error;

import br.compiler.model.CompilationResult;
import br.compiler.model.ErrorMessage;
import br.compiler.model.ErrorSeverity;

public class SemanticErrorHandler extends ErrorHandler {
    
    @Override
    protected ErrorMessage processError(CompilationResult result) {
        if (result.isSuccess()) {
            return null;
        }
        
        String errorType = result.getErrorType();
        if (errorType == null || !errorType.toLowerCase().contains("semântico")
            && !errorType.toLowerCase().contains("semantico")
            && !errorType.toLowerCase().contains("semantic")) {
            return null;
        }
        
        return ErrorMessage.builder()
            .type("SEMÂNTICO")
            .line(result.getErrorLine())
            .position(result.getErrorPosition())
            .message(result.getErrorMessage())
            .severity(ErrorSeverity.ERROR)
            .build();
    }
}

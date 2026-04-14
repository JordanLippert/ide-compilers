package compiler.error;

import compiler.model.CompilationResult;
import compiler.model.ErrorMessage;
import compiler.model.ErrorSeverity;

public class SyntacticErrorHandler extends ErrorHandler {
    
    @Override
    protected ErrorMessage processError(CompilationResult result) {
        if (result.isSuccess()) {
            return null;
        }
        
        String errorType = result.getErrorType();
        if (errorType == null || !errorType.toLowerCase().contains("sintático")
            && !errorType.toLowerCase().contains("sintatico")
            && !errorType.toLowerCase().contains("syntactic")) {
            return null;
        }
        
        return ErrorMessage.builder()
            .type("SINTÁTICO")
            .line(result.getErrorLine())
            .position(result.getErrorPosition())
            .message(result.getErrorMessage())
            .severity(ErrorSeverity.ERROR)
            .build();
    }
}

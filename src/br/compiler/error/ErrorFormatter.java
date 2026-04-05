package br.compiler.error;

import br.compiler.model.CompilationResult;
import br.compiler.model.ErrorMessage;

public class ErrorFormatter extends ErrorHandler {
    
    @Override
    protected ErrorMessage processError(CompilationResult result) {
        if (result.isSuccess()) {
            return null;
        }
        
        // Se já foi processado por um handler anterior
        ErrorMessage msg = ErrorMessage.builder()
            .type(result.getErrorType() != null ? result.getErrorType() : "ERRO")
            .line(result.getErrorLine())
            .position(result.getErrorPosition())
            .message(result.getErrorMessage() != null ? result.getErrorMessage() : "Erro desconhecido")
            .build();
        
        String formatted = String.format("[%s] Linha %d: %s",
            msg.getType(),
            msg.getLine(),
            msg.getMessage()
        );
        
        msg.setFormattedMessage(formatted);
        return msg;
    }
}

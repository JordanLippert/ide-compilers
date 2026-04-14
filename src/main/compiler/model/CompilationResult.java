package compiler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsula o resultado de uma compilação
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class CompilationResult {
    private final boolean success;
    private final String errorType;
    private final String errorMessage;
    private final int errorPosition;
    private final List<Object> data;
    private final Exception exception;

    private CompilationResult(boolean success, String errorType, String errorMessage, 
                              int errorPosition, List<Object> data, Exception exception) {
        this.success = success;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.errorPosition = errorPosition;
        this.data = data;
        this.exception = exception;
    }

    public static CompilationResult success() {
        return new CompilationResult(true, null, null, -1, new ArrayList<>(), null);
    }

    public static CompilationResult success(List<Object> data) {
        return new CompilationResult(true, null, null, -1, data, null);
    }

    public static CompilationResult error(String errorType, String errorMessage, int errorPosition) {
        return new CompilationResult(false, errorType, errorMessage, errorPosition, new ArrayList<>(), null);
    }

    public static CompilationResult error(ErrorMessage errorMessage) {
        return new CompilationResult(false, errorMessage.getType(), errorMessage.getMessage(), 
                                     errorMessage.getPosition(), new ArrayList<>(), null);
    }

    public static CompilationResult exception(Exception exception) {
        return new CompilationResult(false, "EXCEPTION", exception.getMessage(), 
                                     -1, new ArrayList<>(), exception);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorPosition() {
        return errorPosition;
    }

    public List<Object> getData() {
        return data;
    }

    public Exception getException() {
        return exception;
    }

    public int getErrorLine() {
        // Simplificado: retorna position como linha
        // Em implementação real, calcularia a linha baseada na posição no texto
        return errorPosition >= 0 ? errorPosition : 1;
    }

    @Override
    public String toString() {
        if (success) {
            return "Compilação bem-sucedida";
        } else {
            return String.format("Erro [%s]: %s (posição %d)", errorType, errorMessage, errorPosition);
        }
    }
}

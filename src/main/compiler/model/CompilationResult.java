package compiler.model;

import java.util.ArrayList;
import java.util.Collections;
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
    private final List<String> warnings;
    private final List<Object[]> symbolTableRows;

    private CompilationResult(boolean success, String errorType, String errorMessage,
                              int errorPosition, List<Object> data, Exception exception,
                              List<String> warnings, List<Object[]> symbolTableRows) {
        this.success = success;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.errorPosition = errorPosition;
        this.data = data;
        this.exception = exception;
        this.warnings = warnings != null ? warnings : new ArrayList<>();
        this.symbolTableRows = symbolTableRows != null ? symbolTableRows : new ArrayList<>();
    }

    // ----------------------------------------------------------------
    // Factory methods
    // ----------------------------------------------------------------

    public static CompilationResult success() {
        return new CompilationResult(true, null, null, -1,
                new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>());
    }

    public static CompilationResult success(List<Object> data) {
        return new CompilationResult(true, null, null, -1,
                data, null, new ArrayList<>(), new ArrayList<>());
    }

    public static CompilationResult success(List<String> warnings, List<Object[]> symbolTableRows) {
        return new CompilationResult(true, null, null, -1,
                new ArrayList<>(), null, warnings, symbolTableRows);
    }

    public static CompilationResult error(String errorType, String errorMessage, int errorPosition) {
        return new CompilationResult(false, errorType, errorMessage, errorPosition,
                new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>());
    }

    public static CompilationResult error(ErrorMessage errorMessage) {
        return new CompilationResult(false, errorMessage.getType(), errorMessage.getMessage(),
                errorMessage.getPosition(), new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>());
    }

    public static CompilationResult exception(Exception exception) {
        return new CompilationResult(false, "EXCEPTION", exception.getMessage(),
                -1, new ArrayList<>(), exception, new ArrayList<>(), new ArrayList<>());
    }

    // ----------------------------------------------------------------
    // Getters
    // ----------------------------------------------------------------

    public boolean isSuccess() { return success; }
    public String getErrorType() { return errorType; }
    public String getErrorMessage() { return errorMessage; }
    public int getErrorPosition() { return errorPosition; }
    public List<Object> getData() { return data; }
    public Exception getException() { return exception; }
    public List<String> getWarnings() { return Collections.unmodifiableList(warnings); }
    public List<Object[]> getSymbolTableRows() { return Collections.unmodifiableList(symbolTableRows); }

    public int getErrorLine() {
        return errorPosition >= 0 ? errorPosition : 1;
    }

    @Override
    public String toString() {
        if (success) return "Compilação bem-sucedida";
        return String.format("Erro [%s]: %s (posição %d)", errorType, errorMessage, errorPosition);
    }
}

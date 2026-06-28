package compiler.models;

import compiler.enums.ErrorSeverity;
import gals.AnalysisError;

public class CompilationError {
    private final String type;
    private final String message;
    private final ErrorSeverity severity;
    private final int position;
    private final int line;

    private CompilationError(String type, String message, int position, int line, ErrorSeverity severity) {
        this.type = type;
        this.message = message;
        this.severity = severity;
        this.position = position;
        this.line = line;
    }

    public static CompilationError FromError(AnalysisError analysisError) {
        return new CompilationError(
                "AnalysisError",
                analysisError.getMessage(),
                analysisError.getPosition(),
                1,
                ErrorSeverity.ERROR
        );
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public int getPosition() {
        return position;
    }

    public int getLine() {
        return line;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return String.format("[%s] Linha %d: %s", type, line, message);
    }
}

package compiler.models;

public class CompilationWarning {
    private final String message;

    public CompilationWarning(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
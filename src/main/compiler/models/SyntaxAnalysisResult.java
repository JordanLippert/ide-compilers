package compiler.models;

import java.util.List;

public class SyntaxAnalysisResult {
    private final List<SyntacticError> errors;

    public SyntaxAnalysisResult(List<SyntacticError> errors) {
        this.errors = errors;
    }

    public static SyntaxAnalysisResult success()
    {
        return new SyntaxAnalysisResult(List.of());
    }

    public static SyntaxAnalysisResult error(List<SyntacticError> errors)
    {
        return new SyntaxAnalysisResult(errors);
    }

    public List<SyntacticError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
package compiler.models;

import java.util.List;

public class LexicalAnalysisResult {
    private List<Token> tokens;
    private List<LexicalError> errors;

    public LexicalAnalysisResult(List<Token> tokens) {
        this.tokens = tokens;
    }

    public LexicalAnalysisResult(List<Token> tokens, List<LexicalError> errors) {
        this.tokens = tokens;
        this.errors = errors;
    }

    public static LexicalAnalysisResult success(List<Token> tokens) {
        return new LexicalAnalysisResult(tokens);
    }

    public static LexicalAnalysisResult error(List<Token> tokens, List<LexicalError> errors) {
        return new LexicalAnalysisResult(tokens, errors);
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<LexicalError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
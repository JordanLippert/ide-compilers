package compiler.models;

import java.util.List;

public class SemanticAnalysisResult {
    private List<Symbol> symbols;
    private List<SemanticError> errors;

    public SemanticAnalysisResult(List<Symbol> symbols, List<SemanticError> errors) {
        this.symbols = symbols;
        this.errors = errors;
    }

    public static SemanticAnalysisResult success(List<Symbol> symbols)
    {
            return new SemanticAnalysisResult(symbols, List.of());
    }

    public static SemanticAnalysisResult error(List<SemanticError> errors)
    {
        return new SemanticAnalysisResult(List.of(), errors);
    }

    public List<SemanticError> getErrors() {
        return errors;
    }

    public List<Symbol> getSymbols() { return symbols; }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
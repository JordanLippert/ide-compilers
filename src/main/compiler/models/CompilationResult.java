package compiler.models;

import gals.AnalysisError;

import java.util.Collections;
import java.util.List;

/**
 * Encapsula o resultado de uma compilação
 *
 * @author Jordan Lippert
 * @author André Melo
 */
public class CompilationResult {
    private final String intermediateCode;
    private final List<Token> tokens;
    private final List<Symbol> symbolTables;
    private final List<CompilationError> errors;
    private final List<CompilationWarning> warnings;

    public CompilationResult(String intermediateCode, List<Token> tokens, List<Symbol> symbolsTables, List<CompilationError> errors, List<CompilationWarning> warnings) {
        this.intermediateCode = intermediateCode;
        this.errors = List.copyOf(errors);
        this.warnings = List.copyOf(warnings);
        this.symbolTables = List.copyOf(symbolsTables);
        this.tokens = List.copyOf(tokens);
    }

    public static CompilationResult error(AnalysisError error){
        return new CompilationResult(
                "",
                List.of(),
                List.of(),
                List.of(CompilationError.FromError(error)),
                List.of()
        );
    }

    public boolean isSuccess()
    {
        return errors.isEmpty();
    }

    public List<CompilationError> getErrors()
    {
        return Collections.unmodifiableList(errors);
    }

    public List<CompilationWarning> getWarnings()
    {
        return Collections.unmodifiableList(warnings);
    }

    public List<Symbol> getSymbolTable()
    {
        return Collections.unmodifiableList(symbolTables);
    }

    public String getIntermediateCode()
    {
        return intermediateCode;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        // Adiciona os avisos
        for (CompilationWarning warning : warnings) {
            stringBuilder.append(warning).append("\n");
        }

        // Verifica se a compilação foi um sucesso ou não
        if (isSuccess()) {
            stringBuilder.append("Success");
        } else {
            for (CompilationError error : errors) {
                stringBuilder.append(error).append("\n");
            }
        }

        return stringBuilder.toString();
    }
}
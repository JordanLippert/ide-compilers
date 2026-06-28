package compiler.models;

import java.util.ArrayList;
import java.util.List;

public class CompilationContext {
    private String sourceCode;
    private List<Token> tokens;
    private List<Symbol> symbols;
    private String intermediateCode;
//    private ASTNode ast;
//    private SymbolTable symbolTable;
//    private IntermediateProgram intermediateCode;

    private List<CompilationError> errors = new ArrayList<>();
    private List<CompilationWarning> warnings = new ArrayList<>();

    public CompilationContext(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public String getIntermediateCode() {
        return intermediateCode;
    }

    public List<CompilationError> getErrors() {
        return errors;
    }

    public List<CompilationWarning> getWarnings() {
        return warnings;
    }

    // getters/setters
}
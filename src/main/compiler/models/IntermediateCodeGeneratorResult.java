package compiler.models;

import java.util.List;

public class IntermediateCodeGeneratorResult {
    private String generatedCode;
    private List<CodeGenerationError> errors;

    public IntermediateCodeGeneratorResult(String generatedCode, List<CodeGenerationError> errors) {
        this.generatedCode = generatedCode;
        this.errors = errors;
    }

    public List<CodeGenerationError> getErrors() {
        return errors;
    }

    public String getGeneratedCode() { return generatedCode; }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
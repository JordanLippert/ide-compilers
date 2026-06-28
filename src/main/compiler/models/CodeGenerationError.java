package compiler.models;

import gals.AnalysisError;

public class CodeGenerationError extends AnalysisError {
    public CodeGenerationError(String msg) {
        super(msg);
    }

    public CodeGenerationError(String msg, int position) {
        super(msg, position);
    }
}

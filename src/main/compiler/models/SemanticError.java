package compiler.models;

public class SemanticError extends gals.SemanticError {

    public SemanticError(String msg, int position) {
        super(msg, position);
    }

    public SemanticError(String msg) {
        super(msg);
    }
}

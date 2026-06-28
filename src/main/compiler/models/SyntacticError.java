package compiler.models;

public class SyntacticError extends gals.SyntacticError{
    public SyntacticError(String msg, int position) {
        super(msg, position);
    }

    public SyntacticError(String msg) {
        super(msg);
    }
}

package compiler.models;

public class LexicalError extends gals.LexicalError{
    public LexicalError(String msg, int position) {
        super(msg, position);
    }

    public LexicalError(String msg) {
        super(msg);
    }
}

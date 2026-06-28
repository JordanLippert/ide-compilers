package compiler.models;

public class Scope {
    public String name;
    public Scope parentScope;
    public boolean isClosed;

    public Scope(String name, Scope parentScope, boolean isClosed) {
        this.name = name;
        this.parentScope = parentScope;
        this.isClosed = isClosed;
    }
}

package compiler.gals;

public class Scope {
    public String name;
    public Scope parentScope;

    public Scope(String name, Scope parentScope) {
        this.name = name;
        this.parentScope = parentScope;
    }
}

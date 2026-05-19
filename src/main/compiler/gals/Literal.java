package compiler.gals;

public class Literal {
    public SymbolType type;
    public Object value;

    public Literal(SymbolType type, Object value) {
        this.type = type;
        this.value = value;
    }
}

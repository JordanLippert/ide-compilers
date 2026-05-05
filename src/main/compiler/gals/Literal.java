package compiler.gals;

public class Literal {
    public SymbolType type;
    public String value;

    public Literal(SymbolType type, String value) {
        this.type = type;
        this.value = value;
    }
}

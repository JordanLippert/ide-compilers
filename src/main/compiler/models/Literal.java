package compiler.models;

import compiler.enums.SymbolType;

public class Literal {
    public SymbolType type;
    public Object value;

    public Literal(SymbolType type, Object value) {
        this.type = type;
        this.value = value;
    }
}

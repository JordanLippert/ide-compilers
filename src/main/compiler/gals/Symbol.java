package compiler.gals;

public class Symbol {
    public String id;
    public SymbolType type;
    public Scope scope;
    public Object initialValue;
    public Object value;
    public Boolean isAlredyInitialized = false;
    public Boolean isAlredyUsed = false;
    public Boolean isParameter = false;
    public Integer paramterPosition = 0;
    public Boolean isArray = false;
    public Integer arraySize = null;
    public Boolean isMatrix = false;
    public Boolean isByReference = false;
    public Boolean isFunction = false;

    public Symbol(String id, SymbolType type, Scope scope)
    {
        this.id = id;
        this.type = type;
        this.scope = scope;
    }
}

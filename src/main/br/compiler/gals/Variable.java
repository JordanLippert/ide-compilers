package br.compiler.gals;

public class Variable {

    private final Type type;
    private Object value;
    private Object[] array;
    private final boolean isArray;

    public Variable(Type type, Object value, boolean isArray) {
        this.type = type;
        this.isArray = isArray;

        if (isArray) {
            this.array = null;
        } else {
            this.value = value;
        }
    }

    public void setArraySize(int size) {
        this.array = new Object[size];
    }

    public void setArrayValue(int index, Object value) {
        this.array[index] = value;
    }

    public Object getArrayValue(int index) {
        return this.array[index];
    }

    public boolean isArray() {
        return isArray;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
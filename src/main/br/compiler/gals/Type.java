package br.compiler.gals;

public enum Type {
    INT, FLOAT, DOUBLE, BOOL, CHAR, STRING;

    public static Type FromString(String value) {
        return switch (value) {
            case "int" -> INT;
            case "float" -> FLOAT;
            case "double" -> DOUBLE;
            case "bool" -> BOOL;
            case "char" -> CHAR;
            case "string" -> STRING;
            default -> throw new IllegalArgumentException("Invalid type: " + value);
        };
    }
}
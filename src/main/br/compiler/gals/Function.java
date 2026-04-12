package br.compiler.gals;

import java.util.List;

class Function {
    String name;
    List<String> params;
    boolean hasReturn;

    public Function(String name, List<String> params, boolean hasReturn) {
        this.name = name;
        this.params = params;
        this.hasReturn = hasReturn;
    }
}
package br.compiler.gals;

import java.util.List;

abstract class Function {
    String name;
    int numberOfParameters;

    public Function(String name, int numberOfParameters) {
        this.name = name;
        this.numberOfParameters = numberOfParameters;
    }

    abstract Object execute(List<Object> args) throws SemanticError;
}
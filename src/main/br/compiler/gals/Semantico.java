package br.compiler.gals;

import java.util.Stack;

public class Semantico implements Constants
{
    private final Stack<Object> stack = new Stack<>();

    public void executeAction(int action, Token token)	throws SemanticError
    {
        switch (action) {
            case 1: // add literal to stack
            {
                stack.push(token.getLexeme());
            }
            default:
                throw new SemanticError("Semantico: Invalid action " + action);
        }
    }
}
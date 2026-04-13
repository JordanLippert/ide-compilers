package br.compiler.adapter;

import br.compiler.gals.*;
import br.compiler.model.CompilationResult;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação temporária do GalsAdapter
 * Será substituída quando as classes GALS forem geradas
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class GalsParserAdapter implements IGalsAdapter {
    private List<Object> tokens = new ArrayList<>();
    private final Semantico _semantico;
    private final Sintatico _sintatico;

    public GalsParserAdapter(Sintatico sintatico, Semantico semantico) {
        this._sintatico = sintatico;
        this._semantico = semantico;
    }
    
    @Override
    public CompilationResult performLexicalAnalysis(String sourceCode) {
        // TODO: Implementar quando classes GALS forem geradas
        // Por enquanto, apenas simula sucesso
        return CompilationResult.success();
    }
    
    @Override
    public CompilationResult performSyntacticAnalysis(String sourceCode) throws LexicalError, SyntacticError, SemanticError {
        Lexico lexico = new Lexico(sourceCode);
        _sintatico.parse(lexico, _semantico);

        // TODO: add return data in the compilation result to display in console
        return CompilationResult.success();
    }

    @Override
    public List<Object> getTokens() {
        return new ArrayList<>(tokens);
    }
}

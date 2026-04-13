package br.compiler.adapter;

import br.compiler.gals.*;
import br.compiler.model.CompilationResult;
import br.compiler.model.ErrorMessage;

import java.io.StringReader;
import java.nio.channels.ScatteringByteChannel;
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
    private String sourceCode;
    private List<Token> tokens = new ArrayList<>();

    private final Semantico _semantico;
    private final Sintatico _sintatico;

    public GalsParserAdapter(Sintatico sintatico, Semantico semantico) {
        this._sintatico = sintatico;
        this._semantico = semantico;
    }

    @Override
    public CompilationResult performLexicalAnalysis(String sourceCode) {
        this.sourceCode = sourceCode;
        tokens.clear();

        try {
            Lexico lexico = new Lexico(sourceCode);
            Token token;

            while ((token = lexico.nextToken()) != null) {
                tokens.add(token);
            }

            return CompilationResult.success(new ArrayList<>(tokens));
        } catch (Exception e) {
            return CompilationResult.error(mapError(e));
        }
    }

    @Override
    public CompilationResult performSyntacticAnalysis(String sourceCode) {
        this.sourceCode = sourceCode;

        try {
            Lexico lexico = new Lexico(sourceCode);
            Semantico dummy = new Semantico();

            // TODO: Syntactic analysis may also execute semantic actions. But there may be a way to separate it
            _sintatico.parse(lexico, dummy);

            return CompilationResult.success();
        } catch (Exception e) {
            return CompilationResult.error(mapError(e));
        }
    }

    @Override
    public CompilationResult performSemanticAnalysis(String sourceCode) {
        this.sourceCode = sourceCode;

        try {
            Lexico lexico = new Lexico(sourceCode);
            _sintatico.parse(lexico, _semantico);

            return CompilationResult.success();
        } catch (Exception e) {
            return CompilationResult.error(mapError(e));
        }
    }

    @Override
    public List<Object> getTokens() {
        return new ArrayList<>(tokens);
    }

    private ErrorMessage mapError(Exception e) {

        ErrorMessage.Builder builder = ErrorMessage.builder();

        if (e instanceof AnalysisError ae) {
            builder
                .message(ae.getMessage())
                .position(ae.getPosition())
                .line(calculateLine(ae.getPosition()));

            if (e instanceof LexicalError) {
                builder.type("LEXICAL");
            } else if (e instanceof SyntacticError) {
                builder.type("SYNTACTIC");
            } else if (e instanceof SemanticError) {
                builder.type("SEMANTIC");
            } else {
                builder.type("ANALYSIS");
            }

            return builder.build();
        }

        // fallback for non-GALS exceptions
        return ErrorMessage.builder()
                .type("UNKNOWN")
                .message(e.getMessage())
                .build();
    }

    private int calculateLine(int position) {
        if (position < 0) return 1;

        int line = 1;
        for (int i = 0; i < position && i < sourceCode.length(); i++) {
            if (sourceCode.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }
}

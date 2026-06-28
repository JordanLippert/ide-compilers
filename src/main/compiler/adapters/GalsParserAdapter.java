package compiler.adapters;

import compiler.interfaces.ILexicalAnalyzer;
import compiler.interfaces.ISemanticAnalyzer;
import compiler.interfaces.ISyntaxAnalyzer;
import compiler.models.*;
import gals.Lexico;
import gals.Semantico;
import gals.Sintatico;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação temporária do GalsAdapter
 * Será substituída quando as classes GALS forem geradas
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class GalsParserAdapter implements ILexicalAnalyzer, ISyntaxAnalyzer, ISemanticAnalyzer {
    Sintatico _sintatico;
    Semantico _semantico;

    public GalsParserAdapter(Sintatico sintatico, Semantico semantico) {
        _sintatico = sintatico;
        _semantico = semantico;
    }

    @Override
    public LexicalAnalysisResult analyzeLexical(String input) {
        List<Token> tokens = new ArrayList<>();
        try {
            Lexico lexico = new Lexico(input);


            Token token = (Token) lexico.nextToken();
            while (token != null) {
                tokens.add(token);
                token = (Token) lexico.nextToken();
            }
        } catch (Exception e) {
            LexicalError error = new LexicalError(e.getMessage());
            LexicalAnalysisResult.error(tokens, List.of(error));
        }

        return LexicalAnalysisResult.success(tokens);
    }

    @Override
    public SemanticAnalysisResult analyzeSemantic(String input) {
        try {
            Lexico lexico = new Lexico(input);

            // TODO: Syntactic analysis may also execute semantic actions. But there may be a way to separate it
            _sintatico.parse(lexico, _semantico);

            return SemanticAnalysisResult.success(_semantico.getSymbolsTable());
        } catch (SemanticError e) {
            return SemanticAnalysisResult.error(List.of(e));
        } catch (gals.LexicalError e) {
            throw new RuntimeException(e);
        } catch (gals.SyntacticError e) {
            throw new RuntimeException(e);
        } catch (gals.SemanticError e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SyntaxAnalysisResult analyzeSyntax(String input) {
        try {
            // Semantic analysis
            Lexico lexico = new Lexico(input);
            _sintatico.parse(lexico, _semantico);
            _semantico.generateWarnings();

            return SyntaxAnalysisResult.success();
        } catch (SyntacticError e) {
            return SyntaxAnalysisResult.error(List.of(e));
        } catch (gals.LexicalError e) {
            throw new RuntimeException(e);
        } catch (gals.SyntacticError e) {
            throw new RuntimeException(e);
        } catch (gals.SemanticError e) {
            throw new RuntimeException(e);
        }
    }
}

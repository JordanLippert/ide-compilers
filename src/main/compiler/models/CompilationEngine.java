package compiler.models;

import compiler.interfaces.ICompilationEngine;
import compiler.phases.IntermediateCodeGeneratorPhase;
import compiler.phases.LexicalAnalysisPhase;
import compiler.phases.SemanticAnalysisPhase;
import compiler.phases.SyntaxAnalysisPhase;

/**
 * Motor de compilação que coordena as fases de análise
 * Implementa o padrão Strategy
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class CompilationEngine implements ICompilationEngine {
    LexicalAnalysisPhase lexicalAnalysisPhase;
    SyntaxAnalysisPhase syntaxAnalysisPhase;
    SemanticAnalysisPhase semanticAnalysisPhase;
    IntermediateCodeGeneratorPhase intermediateCodeGeneratorPhase;

    public CompilationEngine(LexicalAnalysisPhase lexicalAnalysisPhase, SyntaxAnalysisPhase syntaxAnalysisPhase, SemanticAnalysisPhase semanticAnalysisPhase,  IntermediateCodeGeneratorPhase intermediateCodeGeneratorPhase) {
        this.lexicalAnalysisPhase = lexicalAnalysisPhase;
        this.syntaxAnalysisPhase = syntaxAnalysisPhase;
        this.semanticAnalysisPhase = semanticAnalysisPhase;
        this.intermediateCodeGeneratorPhase = intermediateCodeGeneratorPhase;
    }
    
    public CompilationResult compile(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }

        CompilationContext compilationContext = new CompilationContext(code);

        lexicalAnalysisPhase.execute(compilationContext);
        syntaxAnalysisPhase.execute(code);
        semanticAnalysisPhase.execute(code);
        intermediateCodeGeneratorPhase.execute(compilationContext.getTokens(), compilationContext.getSymbols());

        return new CompilationResult(compilationContext.getIntermediateCode(), compilationContext.getTokens(), compilationContext.getSymbols(), compilationContext.getErrors(),  compilationContext.getWarnings());
    }
}

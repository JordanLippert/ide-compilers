package compiler.builders;

import compiler.interfaces.*;
import compiler.models.CompilationEngine;
import compiler.phases.IntermediateCodeGeneratorPhase;
import compiler.phases.LexicalAnalysisPhase;
import compiler.phases.SemanticAnalysisPhase;
import compiler.phases.SyntaxAnalysisPhase;

public class CompilationEngineBuilder {
    ILexicalAnalyzer lexicalAnalyzer;
    ISyntaxAnalyzer syntaxAnalyzer;
    ISemanticAnalyzer semanticAnalyzer;
    IIntermediateCodeGenerator intermediateCodeGenerator;
    ICodeOptimizer codeOptimizer;

    private CompilationEngineBuilder() {}

    public static CompilationEngineBuilder Empty()
    {
        return new CompilationEngineBuilder();
    }

    public CompilationEngineBuilder lexicalAnalyzer(ILexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        return this;
    }

    public CompilationEngineBuilder syntaxAnalyzer(ISyntaxAnalyzer syntaxAnalyzer) {
        this.syntaxAnalyzer = syntaxAnalyzer;
        return this;
    }

    public CompilationEngineBuilder semanticAnalyzer(ISemanticAnalyzer semanticAnalyzer) {
        this.semanticAnalyzer = semanticAnalyzer;
        return this;
    }

    public CompilationEngineBuilder intermediateCodeGenerator(IIntermediateCodeGenerator intermediateCodeGenerator) {
        this.intermediateCodeGenerator = intermediateCodeGenerator;
        return this;
    }

    public CompilationEngineBuilder codeOptimizer(ICodeOptimizer codeOptimizer) {
        this.codeOptimizer = codeOptimizer;
        return this;
    }

    public CompilationEngine build() {

        if (lexicalAnalyzer == null) {
            throw new IllegalStateException("Lexical analyzer is required");
        }

        if (syntaxAnalyzer == null) {
            throw new IllegalStateException("Syntax analyzer is required");
        }

        if (semanticAnalyzer == null) {
            throw new IllegalStateException("Semantic analyzer is required");
        }

        if (intermediateCodeGenerator == null) {
            throw new IllegalStateException("Intermediate code generator is required");
        }

        LexicalAnalysisPhase lexicalAnalysisPhase = new LexicalAnalysisPhase(lexicalAnalyzer);
        SyntaxAnalysisPhase syntaxAnalysisPhase = new SyntaxAnalysisPhase(syntaxAnalyzer);
        SemanticAnalysisPhase semanticAnalysisPhase = new SemanticAnalysisPhase(semanticAnalyzer);
        IntermediateCodeGeneratorPhase intermediateCodeGeneratorPhase = new IntermediateCodeGeneratorPhase(intermediateCodeGenerator);

        // codeOptimizer can be optional (depends on your design)
        return new CompilationEngine(
                lexicalAnalysisPhase,
                syntaxAnalysisPhase,
                semanticAnalysisPhase,
                intermediateCodeGeneratorPhase
        );
    }
}

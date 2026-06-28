package compiler.interfaces;

import compiler.models.SyntaxAnalysisResult;

public interface ISyntaxAnalyzer {
    public SyntaxAnalysisResult analyzeSyntax(String input);
}

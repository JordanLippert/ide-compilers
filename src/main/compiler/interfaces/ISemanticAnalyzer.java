package compiler.interfaces;

import compiler.models.SemanticAnalysisResult;

public interface ISemanticAnalyzer {
    SemanticAnalysisResult analyzeSemantic(String input);
}

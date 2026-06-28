package compiler.interfaces;


import compiler.models.LexicalAnalysisResult;

public interface ILexicalAnalyzer {
    public LexicalAnalysisResult analyzeLexical(String input);
}

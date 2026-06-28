package compiler.interfaces;

import compiler.models.CompilationResult;

public interface ICompilationEngine {
    public CompilationResult compile(String code);
}

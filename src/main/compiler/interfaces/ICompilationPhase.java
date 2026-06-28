package compiler.interfaces;

import compiler.models.CompilationContext;

public interface ICompilationPhase {
    void execute(CompilationContext context);
}
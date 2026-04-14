package compiler.factory;

import compiler.adapter.IGalsAdapter;
import compiler.adapter.GalsParserAdapter;
import compiler.compiler.CompilationEngine;
import compiler.compiler.LexicalPhase;
import compiler.compiler.SemanticPhase;
import compiler.compiler.SyntacticPhase;
import compiler.gals.Semantico;
import compiler.gals.Sintatico;
import compiler.ide.ConsolePanel;

/**
 * Factory para criação de parsers e engine de compilação
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class ParserFactory {
    
    public static IGalsAdapter createGalsAdapter() {
        Sintatico sintatico = new Sintatico();
        Semantico semantico = new Semantico();
        return new GalsParserAdapter(sintatico, semantico);
    }
    
    public static CompilationEngine createCompilationEngine() {
        CompilationEngine engine = new CompilationEngine();

        IGalsAdapter adapter = createGalsAdapter();

        engine.addPhase(new LexicalPhase(adapter));
        // engine.addPhase(new SyntacticPhase(adapter)); remove this for know cause is doing the same thing as the SemanticPhase
        engine.addPhase(new SemanticPhase(adapter));
        
        return engine;
    }
}

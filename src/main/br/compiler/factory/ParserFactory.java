package br.compiler.factory;

import br.compiler.adapter.IGalsAdapter;
import br.compiler.adapter.GalsParserAdapter;
import br.compiler.compiler.CompilationEngine;
import br.compiler.compiler.LexicalPhase;
import br.compiler.compiler.SemanticPhase;
import br.compiler.compiler.SyntacticPhase;
import br.compiler.gals.Semantico;
import br.compiler.gals.Sintatico;
import br.compiler.ide.ConsolePanel;

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

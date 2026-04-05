package br.compiler.factory;

import br.compiler.adapter.GalsAdapter;
import br.compiler.adapter.GalsParserAdapter;
import br.compiler.compiler.CompilationEngine;
import br.compiler.compiler.LexicalPhase;
import br.compiler.compiler.SyntacticPhase;

/**
 * Factory para criação de parsers e engine de compilação
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class ParserFactory {
    
    public static GalsAdapter createGalsAdapter() {
        return new GalsParserAdapter();
    }
    
    public static CompilationEngine createCompilationEngine() {
        CompilationEngine engine = new CompilationEngine();
        GalsAdapter adapter = createGalsAdapter();
        
        engine.addPhase(new LexicalPhase(adapter));
        engine.addPhase(new SyntacticPhase(adapter));
        
        return engine;
    }
}

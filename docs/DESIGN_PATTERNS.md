# Padrões de Design

Este documento detalha os padrões de design utilizados no projeto e suas justificativas.

## 1. Strategy Pattern 🎯

### Problema
As diferentes fases de compilação (léxica, sintática, semântica) têm comportamentos distintos, mas seguem o mesmo fluxo: receber código → processar → retornar resultado/erro.

### Solução
Usar o padrão Strategy para encapsular cada fase de compilação.

### Implementação

```java
// Interface Strategy
public interface CompilationPhase {
    CompilationResult execute(String sourceCode) throws CompilationException;
    String getPhaseName();
}

// Concrete Strategies
public class LexicalPhase implements CompilationPhase {
    private GalsAdapter adapter;
    
    @Override
    public CompilationResult execute(String sourceCode) {
        // Usa o adapter para chamar o Lexico.java gerado pelo GALS
        return adapter.performLexicalAnalysis(sourceCode);
    }
    
    @Override
    public String getPhaseName() {
        return "Análise Léxica";
    }
}

public class SyntacticPhase implements CompilationPhase {
    private GalsAdapter adapter;
    
    @Override
    public CompilationResult execute(String sourceCode) {
        // Usa o adapter para chamar o Sintatico.java
        return adapter.performSyntacticAnalysis(sourceCode);
    }
    
    @Override
    public String getPhaseName() {
        return "Análise Sintática";
    }
}

// Context
public class CompilationEngine {
    private List<CompilationPhase> phases;
    
    public CompilationResult compile(String sourceCode) {
        for (CompilationPhase phase : phases) {
            CompilationResult result = phase.execute(sourceCode);
            if (!result.isSuccess()) {
                return result; // Para na primeira fase com erro
            }
        }
        return CompilationResult.success();
    }
}
```

### Vantagens
- ✅ Fácil adicionar novas fases (ex: otimização)
- ✅ Cada fase pode ser testada isoladamente
- ✅ IDE não precisa conhecer detalhes de cada fase

---

## 2. Adapter Pattern 🔌

### Problema
As classes geradas pelo GALS têm uma interface específica que não queremos expor diretamente para o resto do sistema. Se mudarmos de ferramenta (ex: ANTLR), teríamos que modificar todo o código.

### Solução
Criar um Adapter que traduz a interface do GALS para uma interface genérica do nosso sistema.

### Implementação

```java
// Interface Target (o que nosso sistema espera)
public interface GalsAdapter {
    CompilationResult performLexicalAnalysis(String sourceCode);
    CompilationResult performSyntacticAnalysis(String sourceCode);
    List<Token> getTokens();
}

// Concrete Adapter (adapta as classes GALS)
public class GalsParserAdapter implements GalsAdapter {
    
    @Override
    public CompilationResult performLexicalAnalysis(String sourceCode) {
        try {
            Lexico lexico = new Lexico(sourceCode);
            Token token;
            List<Token> tokens = new ArrayList<>();
            
            while ((token = lexico.nextToken()) != null) {
                tokens.add(token);
            }
            
            return CompilationResult.success(tokens);
            
        } catch (LexicalError e) {
            return CompilationResult.error(
                "Erro Léxico",
                e.getMessage(),
                e.getPosition()
            );
        }
    }
    
    @Override
    public CompilationResult performSyntacticAnalysis(String sourceCode) {
        try {
            Lexico lexico = new Lexico(sourceCode);
            Sintatico sintatico = new Sintatico();
            sintatico.parse(lexico, new Semantico());
            
            return CompilationResult.success();
            
        } catch (LexicalError e) {
            return CompilationResult.error("Erro Léxico", e.getMessage(), e.getPosition());
        } catch (SyntaticError e) {
            return CompilationResult.error("Erro Sintático", e.getMessage(), e.getPosition());
        } catch (SemanticError e) {
            return CompilationResult.error("Erro Semântico", e.getMessage(), e.getPosition());
        }
    }
    
    @Override
    public List<Token> getTokens() {
        // Retorna tokens já processados
        return Collections.unmodifiableList(tokens);
    }
}
```

### Vantagens
- ✅ Isola o sistema das classes GALS
- ✅ Fácil trocar de ferramenta de geração
- ✅ Interface limpa e testável

---

## 3. Chain of Responsibility Pattern ⛓️

### Problema
Diferentes tipos de erros (léxicos, sintáticos, semânticos) precisam ser formatados e tratados de formas diferentes. Alguns erros podem precisar de pós-processamento ou enriquecimento da mensagem.

### Solução
Usar Chain of Responsibility para processar erros em sequência.

### Implementação

```java
// Handler abstrato
public abstract class ErrorHandler {
    protected ErrorHandler next;
    
    public void setNext(ErrorHandler handler) {
        this.next = handler;
    }
    
    public ErrorMessage handle(CompilationResult result) {
        ErrorMessage message = processError(result);
        
        if (next != null) {
            return next.handle(result);
        }
        
        return message;
    }
    
    protected abstract ErrorMessage processError(CompilationResult result);
}

// Concrete Handlers
public class LexicalErrorHandler extends ErrorHandler {
    @Override
    protected ErrorMessage processError(CompilationResult result) {
        if (!result.getErrorType().equals("Erro Léxico")) {
            return null; // Não é minha responsabilidade
        }
        
        return ErrorMessage.builder()
            .type("LÉXICO")
            .line(result.getErrorPosition())
            .message("Token inválido: " + result.getErrorMessage())
            .severity(ErrorSeverity.ERROR)
            .build();
    }
}

public class SyntacticErrorHandler extends ErrorHandler {
    @Override
    protected ErrorMessage processError(CompilationResult result) {
        if (!result.getErrorType().equals("Erro Sintático")) {
            return null;
        }
        
        return ErrorMessage.builder()
            .type("SINTÁTICO")
            .line(result.getErrorPosition())
            .message("Estrutura inválida: " + result.getErrorMessage())
            .severity(ErrorSeverity.ERROR)
            .build();
    }
}

public class ErrorFormatter extends ErrorHandler {
    @Override
    protected ErrorMessage processError(CompilationResult result) {
        // Último elo: formata para exibição
        ErrorMessage msg = result.getErrorMessage();
        
        return ErrorMessage.builder()
            .formattedMessage(
                String.format("[%s] Linha %d: %s",
                    msg.getType(),
                    msg.getLine(),
                    msg.getMessage()
                )
            )
            .build();
    }
}

// Uso
ErrorHandler chain = new LexicalErrorHandler();
chain.setNext(new SyntacticErrorHandler());
chain.setNext(new SemanticErrorHandler());
chain.setNext(new ErrorFormatter());

ErrorMessage formatted = chain.handle(compilationResult);
console.display(formatted.getFormattedMessage());
```

### Vantagens
- ✅ Fácil adicionar novos tipos de erro
- ✅ Cada handler tem responsabilidade única
- ✅ Ordem de processamento configurável

---

## 4. Factory Pattern 🏭

### Problema
Criar componentes da IDE e instanciar parsers requer lógica de criação que não deveria estar espalhada pelo código.

### Solução
Centralizar criação em factories.

### Implementação

```java
// Factory para componentes da IDE
public class ComponentFactory {
    
    public static JTextPane createEditor() {
        JTextPane editor = new JTextPane();
        editor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        editor.setBackground(Color.WHITE);
        editor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return editor;
    }
    
    public static JTextArea createConsole() {
        JTextArea console = new JTextArea();
        console.setFont(new Font("Monospaced", Font.PLAIN, 14));
        console.setEditable(false);
        console.setBackground(new Color(40, 40, 40));
        console.setForeground(Color.WHITE);
        return console;
    }
    
    public static JButton createCompileButton() {
        JButton button = new JButton("Compilar");
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }
}

// Factory para parsers
public class ParserFactory {
    
    public static GalsAdapter createGalsAdapter() {
        return new GalsParserAdapter();
    }
    
    public static CompilationEngine createCompilationEngine() {
        CompilationEngine engine = new CompilationEngine();
        engine.addPhase(new LexicalPhase(createGalsAdapter()));
        engine.addPhase(new SyntacticPhase(createGalsAdapter()));
        return engine;
    }
}
```

### Vantagens
- ✅ Criação centralizada
- ✅ Fácil mudar configurações
- ✅ Reutilização de código

---

## 📊 Diagrama de Interação dos Padrões

```
┌──────────────┐
│  CompilerIDE │ (usa Factory para criar componentes)
└──────┬───────┘
       │
       ↓ (delega compilação)
┌──────────────────┐
│ CompilationEngine│ (Strategy - orquestra fases)
└──────┬───────────┘
       │
       ↓ (usa para cada fase)
┌──────────────────┐
│  GalsAdapter     │ (Adapter - isola GALS)
└──────┬───────────┘
       │
       ↓ (se houver erro)
┌──────────────────┐
│  ErrorHandler    │ (Chain - processa erros)
└──────────────────┘
```

## 🎓 Conclusão

A combinação desses padrões resulta em um sistema:
- **Flexível:** Fácil adicionar novas fases, erros, componentes
- **Manutenível:** Responsabilidades bem definidas
- **Testável:** Cada componente pode ser testado isoladamente
- **Desacoplado:** Mudanças em um componente não afetam outros

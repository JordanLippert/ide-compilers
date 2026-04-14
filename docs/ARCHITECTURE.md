# Arquitetura do Sistema

## 🏗️ Visão Geral

O sistema é dividido em três camadas principais:

```
┌─────────────────────────────────────────┐
│         CAMADA DE APRESENTAÇÃO          │
│              (IDE - GUI)                │
│  - Editor de Código                     │
│  - Console de Mensagens                 │
│  - Barra de Menu                        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       CAMADA DE LÓGICA DE NEGÓCIO       │
│        (Compilation Engine)             │
│  - Strategy Pattern                     │
│  - Chain of Responsibility              │
│  - Factory Pattern                      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         CAMADA DE ANÁLISE               │
│         (Classes GALS)                  │
│  - Analisador Léxico                    │
│  - Analisador Sintático                 │
│  - Analisador Semântico (futuro)        │
└─────────────────────────────────────────┘
```

## 🎯 Componentes Principais

### 1. IDE (Interface Gráfica)

**Responsabilidade:** Interação com o usuário

**Componentes:**
- `CompilerIDE.java` - Janela principal da aplicação
- `EditorPanel.java` - Área de edição de código (JTextPane com fonte 14)
- `ConsolePanel.java` - Console de mensagens (JTextArea com fonte 14)
- `MenuBar.java` - Menu com opções (Novo, Abrir, Salvar, Compilar)

**Tecnologia:** Java Swing

### 2. Compilation Engine

**Responsabilidade:** Orquestrar o processo de compilação

**Componentes:**
- `CompilationEngine.java` - Controlador principal que executa as fases
- `CompilationPhase.java` - Interface Strategy para cada fase
- `LexicalPhase.java` - Executa análise léxica
- `SyntacticPhase.java` - Executa análise sintática
- `SemanticPhase.java` - Executa análise semântica (futuro)

**Padrão:** Strategy Pattern

### 3. Adapter Layer

**Responsabilidade:** Desacoplar a IDE das classes geradas pelo GALS

**Componentes:**
- `GalsAdapter.java` - Interface genérica para adaptação
- `GalsParserAdapter.java` - Implementação concreta do adapter

**Padrão:** Adapter Pattern

**Vantagem:** Se mudarmos a ferramenta de geração (ex: ANTLR), só precisamos criar um novo adapter.

### 4. Error Handling System

**Responsabilidade:** Processar e formatar erros de compilação

**Componentes:**
- `ErrorHandler.java` - Interface do Chain of Responsibility
- `ErrorFormatter.java` - Formata mensagens para exibição
- `LexicalErrorHandler.java` - Trata erros léxicos
- `SyntacticErrorHandler.java` - Trata erros sintáticos
- `SemanticErrorHandler.java` - Trata erros semânticos

**Padrão:** Chain of Responsibility

**Fluxo:**
```
Erro detectado → LexicalErrorHandler → SyntacticErrorHandler 
    → SemanticErrorHandler → ErrorFormatter → Console
```

### 5. Factory Layer

**Responsabilidade:** Criar instâncias de componentes

**Componentes:**
- `ComponentFactory.java` - Cria componentes da IDE (editor, console)
- `ParserFactory.java` - Cria instâncias dos analisadores

**Padrão:** Factory Pattern

### 6. GALS Generated Classes

**Responsabilidade:** Realizar análise léxica e sintática

**Componentes (gerados automaticamente):**
- `Lexico.java` - Scanner/Lexer
- `Sintatico.java` - Parser
- `Semantico.java` - Semantic Analyzer
- `Token.java` - Classe de token
- `LexicalError.java` - Exceção léxica
- `SyntaticError.java` - Exceção sintática
- `SemanticError.java` - Exceção semântica

**Observação:** Essas classes não devem ser editadas manualmente!

## 🔄 Fluxo de Compilação

```
1. Usuário digita código no EditorPanel
2. Usuário clica em "Compilar"
3. CompilerIDE captura o texto do editor
4. CompilationEngine é invocado
5. LexicalPhase processa através do GalsParserAdapter
6. Se houver erros:
   a. Erro passa pelo Chain of Responsibility
   b. Para erro sintático, o `GalsParserAdapter` enriquece a mensagem com token encontrado e tokens esperados
   c. ErrorFormatter formata a mensagem
   d. Mensagem é exibida no ConsolePanel
7. Se não houver erros léxicos:
   a. SyntacticPhase é executada
   b. Repete processo de erro se necessário
8. Resultado final é mostrado ao usuário
```

## 🎨 Princípios de Design Aplicados

### SOLID

- **S** - Single Responsibility: Cada classe tem uma responsabilidade única
- **O** - Open/Closed: Fácil extensão via Strategy e Chain
- **L** - Liskov Substitution: Interfaces bem definidas
- **I** - Interface Segregation: Interfaces específicas e coesas
- **D** - Dependency Inversion: Dependência de abstrações (interfaces)

### Separation of Concerns

- GUI separada da lógica de compilação
- Lógica de compilação separada das classes GALS
- Sistema de erros independente

### Low Coupling / High Cohesion

- Componentes fracamente acoplados via interfaces
- Cada pacote tem alta coesão interna

## 📊 Diagrama de Dependências

```
IDE Layer
    ↓ (usa)
Compiler Layer
    ↓ (usa)
Adapter Layer
    ↓ (adapta)
GALS Layer
```

**Nota:** As setas representam dependência. Camadas superiores dependem das inferiores, mas não vice-versa.

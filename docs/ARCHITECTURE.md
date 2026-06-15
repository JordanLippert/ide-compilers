# Arquitetura do Sistema

## 🏗️ Visão Geral

O sistema é dividido em quatro camadas principais:

```
┌─────────────────────────────────────────┐
│         CAMADA DE APRESENTAÇÃO          │
│              (IDE - GUI)                │
│  - Editor de Código                     │
│  - Console de Mensagens                 │
│  - Tabela de Símbolos                   │
│  - Painel de Código ASM                 │
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
│  - Analisador Semântico                 │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       CAMADA DE GERAÇÃO DE CÓDIGO       │
│         (codegen)                       │
│  - Gerador BIP Assembly                 │
└─────────────────────────────────────────┘
```

## 🎯 Componentes Principais

### 1. IDE (Interface Gráfica)

**Responsabilidade:** Interação com o usuário

**Componentes:**
- `CompilerIDE.java` - Janela principal da aplicação
- `EditorPanel.java` - Área de edição de código (JTextPane com fonte 14)
- `ConsolePanel.java` - Console de mensagens (JTextArea com fonte 14)
- `SymbolTablePanel.java` - Tabela de símbolos (JTable)
- `AsmPanel.java` - Painel de exibição do código assembly BIP (read-only, aba ao lado da tabela de símbolos)
- `MenuBar.java` - Menu com opções (Novo, Abrir, Salvar, Compilar)
- `StatusBar.java` - Barra de status com posição do cursor e nome do arquivo

**Tecnologia:** Java Swing

### 2. Compilation Engine

**Responsabilidade:** Orquestrar o processo de compilação

**Componentes:**
- `CompilationEngine.java` - Controlador principal que executa as fases
- `ICompilationPhase.java` - Interface Strategy para cada fase
- `LexicalPhase.java` - Executa análise léxica
- `SyntacticPhase.java` - Executa análise sintática
- `SemanticPhase.java` - Executa análise semântica + dispara geração de código BIP

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

**Responsabilidade:** Realizar análise léxica, sintática e semântica

**Componentes:**
- `Lexico.java` - Scanner/Lexer (gerado pelo GALS)
- `Sintatico.java` - Parser SLR (gerado pelo GALS)
- `Semantico.java` - Analisador semântico com tabela de símbolos, type checking e constant folding
- `Symbol.java` - Representa símbolo na tabela (tipo, escopo, valor, flags de array/função)
- `Scope.java` - Representa escopo léxico com hierarquia pai
- `Literal.java` - Valor temporário durante análise de expressões
- `Token.java` - Classe de token
- `LexicalError.java` - Exceção léxica
- `SyntacticError.java` - Exceção sintática
- `SemanticError.java` - Exceção semântica

**Observação:** `Lexico.java` e `Sintatico.java` são gerados pelo GALS e não devem ser editados. `Semantico.java` e demais classes de suporte são mantidas manualmente.

### 7. Code Generation

**Responsabilidade:** Gerar código assembly BIP a partir do programa fonte

**Componentes:**
- `BipCodeGenerator.java` - Faz varredura recursiva no stream de tokens para produzir código BIP

**Funcionamento:**
1. Recebe a lista de tokens (da análise léxica) e a tabela de símbolos (da análise semântica)
2. Seção `.data`: gerada a partir da tabela de símbolos (variáveis, arrays com tamanho)
3. Seção `.code`: gerada por parser recursivo-descendente no stream de tokens

**Instruções BIP suportadas:**
- `LDA var` / `LDA #n` — carregar acumulador
- `STA var` — armazenar acumulador
- `ADD`, `SUB` — aritmética
- `AND`, `OR`, `XOR` — operações bit a bit
- `SHL`, `SHR` — deslocamento
- `IN var` — leitura de entrada
- `OUT` — saída do acumulador
- `HLT` — fim de programa
- Indexação de arrays: `LDA v[idx]`, `STA v[idx]`, `IN v[idx]`

## 🔄 Fluxo de Compilação

```
1. Usuário digita código no EditorPanel
2. Usuário clica em "Compilar" (ou F9)
3. CompilerIDE captura o texto do editor
4. CompilationEngine é invocado
5. LexicalPhase — GalsParserAdapter.performLexicalAnalysis()
   → tokeniza o fonte, armazena tokens
6. Se houver erros léxicos:
   → Chain of Responsibility formata e exibe no ConsolePanel
7. SemanticPhase — GalsParserAdapter.performSemanticAnalysis()
   a. Parse completo (Lexico + Sintatico + Semantico)
   b. Semantico constrói tabela de símbolos, faz type checking
   c. Coleta tokens num segundo pass (collectTokens)
   d. BipCodeGenerator gera código BIP a partir dos tokens + tabela de símbolos
   e. Retorna CompilationResult com warnings, symbolTableRows e asmCode
8. Se houver erros semânticos/sintáticos:
   → Chain of Responsibility formata e exibe no ConsolePanel
9. Se sucesso:
   → ConsolePanel exibe mensagem de sucesso + warnings
   → SymbolTablePanel exibe tabela de símbolos
   → AsmPanel exibe código assembly BIP gerado (aba auto-selecionada)
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

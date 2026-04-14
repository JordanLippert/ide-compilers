# Estrutura do Projeto

## 📂 Organização de Diretórios

```
ide-compilers/
├── .git/                           # Controle de versão Git
├── .idea/                          # Configurações do IntelliJ IDEA
├── docs/                           # 📚 Documentação do projeto
│   ├── README.md                   # Índice da documentação
│   ├── ARCHITECTURE.md             # Arquitetura do sistema
│   ├── DESIGN_PATTERNS.md          # Padrões de design utilizados
│   ├── PROJECT_STRUCTURE.md        # Este arquivo
│   ├── GRAMMAR.md                  # Documentação da gramática
│   └── DEVELOPMENT_GUIDE.md        # Guia de desenvolvimento
│
├── src/                            # 💻 Código-fonte Java
│   └── main/
│       ├── Main.java               # Entry point da aplicação
│       │
│       └── compiler/
│           │
│           ├── ide/                # 🖥️ Interface Gráfica
│           │   ├── CompilerIDE.java
│           │   ├── EditorPanel.java
│           │   ├── ConsolePanel.java
│           │   └── MenuBar.java
│           │
│           ├── compiler/           # ⚙️ Engine de Compilação
│           │   ├── CompilationEngine.java
│           │   ├── CompilationPhase.java
│           │   ├── LexicalPhase.java
│           │   ├── SyntacticPhase.java
│           │   └── SemanticPhase.java
│           │
│           ├── gals/               # 🔧 Classes geradas pelo GALS
│           │   ├── Lexico.java
│           │   ├── Sintatico.java
│           │   ├── Semantico.java
│           │   ├── Constants.java
│           │   ├── ParserConstants.java
│           │   ├── ScannerConstants.java
│           │   ├── Token.java
│           │   ├── LexicalError.java
│           │   ├── SyntaticError.java
│           │   └── SemanticError.java
│           │
│           ├── adapter/            # 🔌 Adapter Pattern
│           │   ├── GalsAdapter.java
│           │   └── GalsParserAdapter.java
│           │
│           ├── error/              # 🚨 Sistema de Erros
│           │   ├── ErrorHandler.java
│           │   ├── ErrorFormatter.java
│           │   ├── LexicalErrorHandler.java
│           │   ├── SyntacticErrorHandler.java
│           │   └── SemanticErrorHandler.java
│           │
│           ├── factory/            # 🏭 Factory Pattern
│           │   ├── ComponentFactory.java
│           │   └── ParserFactory.java
│           │
│           └── model/              # 📋 Modelos de Dados
│               ├── CompilationResult.java
│               └── ErrorMessage.java
│
├── resources/                      # 📄 Recursos da aplicação
│   ├── examples/                   # Exemplos de código para teste
│   │   ├── 01_hello_world.txt
│   │   ├── 02_variables.txt
│   │   ├── 03_conditionals.txt
│   │   ├── 04_loops.txt
│   │   ├── 05_functions.txt
│   │   └── 06_complete_example.txt
│   │
│   └── icons/                      # Ícones da IDE (opcional)
│       ├── compile.png
│       ├── new.png
│       ├── open.png
│       └── save.png
│
├── out/                            # 🎯 Arquivos compilados (.class)
│   └── production/
│       └── ide-compilers/
│
├── .gitignore                      # Arquivos ignorados pelo Git
├── ide-compilers.iml               # Configuração do módulo IntelliJ
├── trabalho_compiladores_01.gals   # Gramática GALS (T1 - base)
└── README.md                       # README principal do projeto
```

## 📦 Descrição dos Pacotes

### `compiler.ide` - Interface Gráfica

Contém todos os componentes visuais da IDE.

| Classe | Responsabilidade |
|--------|------------------|
| `CompilerIDE.java` | Janela principal, orquestra os componentes |
| `EditorPanel.java` | Painel de edição de código (JTextPane) |
| `ConsolePanel.java` | Painel de mensagens (JTextArea) |
| `MenuBar.java` | Barra de menu (Novo, Abrir, Salvar, Compilar) |

**Dependências:** `compiler.compiler`, `compiler.factory`

---

### `compiler.compiler` - Engine de Compilação

Implementa o padrão Strategy para as fases de compilação.

| Classe | Responsabilidade |
|--------|------------------|
| `CompilationEngine.java` | Orquestra a execução das fases |
| `CompilationPhase.java` | Interface Strategy |
| `LexicalPhase.java` | Executa análise léxica |
| `SyntacticPhase.java` | Executa análise sintática |
| `SemanticPhase.java` | Executa análise semântica (futuro) |

**Dependências:** `compiler.adapter`, `compiler.model`, `compiler.error`

---

### `compiler.gals` - Classes GALS

Classes geradas automaticamente pelo GALS. **Não editar manualmente!**

| Classe | Responsabilidade |
|--------|------------------|
| `Lexico.java` | Analisador léxico (scanner) |
| `Sintatico.java` | Analisador sintático (parser) |
| `Semantico.java` | Analisador semântico |
| `Token.java` | Representa um token |
| `LexicalError.java` | Exceção de erro léxico |
| `SyntaticError.java` | Exceção de erro sintático |
| `SemanticError.java` | Exceção de erro semântico |
| `Constants.java` | Constantes gerais |
| `ParserConstants.java` | Constantes do parser |
| `ScannerConstants.java` | Constantes do scanner |

**Dependências:** Nenhuma (standalone)

**Regeneração:** Sempre que a gramática `.gals` for modificada

---

### `compiler.adapter` - Adapter Pattern

Isola o sistema das classes geradas pelo GALS.

| Classe | Responsabilidade |
|--------|------------------|
| `GalsAdapter.java` | Interface do adapter |
| `GalsParserAdapter.java` | Implementação concreta |

**Dependências:** `compiler.gals`, `compiler.model`

---

### `compiler.error` - Sistema de Erros

Implementa Chain of Responsibility para tratamento de erros.

| Classe | Responsabilidade |
|--------|------------------|
| `ErrorHandler.java` | Interface do Chain |
| `ErrorFormatter.java` | Formata mensagens para exibição |
| `LexicalErrorHandler.java` | Processa erros léxicos |
| `SyntacticErrorHandler.java` | Processa erros sintáticos |
| `SemanticErrorHandler.java` | Processa erros semânticos |

**Dependências:** `compiler.model`

---

### `compiler.factory` - Factory Pattern

Centraliza criação de objetos.

| Classe | Responsabilidade |
|--------|------------------|
| `ComponentFactory.java` | Cria componentes da IDE |
| `ParserFactory.java` | Cria parsers e adapters |

**Dependências:** Múltiplas (cria objetos de vários pacotes)

---

### `compiler.model` - Modelos de Dados

DTOs e classes de modelo.

| Classe | Responsabilidade |
|--------|------------------|
| `CompilationResult.java` | Encapsula resultado da compilação |
| `ErrorMessage.java` | Representa uma mensagem de erro |

**Dependências:** Nenhuma (POJOs)

---

## 🔄 Fluxo de Dependências

```
Main.java
    ↓
CompilerIDE (ide)
    ↓
CompilationEngine (compiler)
    ↓
GalsAdapter (adapter)
    ↓
Classes GALS (gals)

            ↓ (em caso de erro)
        
ErrorHandler (error)
    ↓
ConsolePanel (ide)
```

**Princípio:** Dependências fluem de cima para baixo, nunca no sentido contrário.

---

## 📝 Convenções de Nomenclatura

### Pacotes
- Tudo em **minúsculas**
- Nomes descritivos e concisos
- Ex: `ide`, `compiler`, `error`

### Classes
- **PascalCase**
- Substantivos descritivos
- Ex: `CompilerIDE`, `ErrorHandler`

### Interfaces
- **PascalCase**
- Frequentemente adjetivos ou capacidades
- Ex: `CompilationPhase`, `GalsAdapter`

### Métodos
- **camelCase**
- Verbos ou ações
- Ex: `compile()`, `performAnalysis()`, `formatError()`

### Constantes
- **UPPER_SNAKE_CASE**
- Ex: `MAX_ERRORS`, `DEFAULT_FONT_SIZE`

---

## 🎯 Arquivos Importantes

| Arquivo | Descrição |
|---------|-----------|
| `trabalho_compiladores_01.gals` | Gramática da linguagem (T1 como base) |
| `Main.java` | Entry point da aplicação |
| `.gitignore` | Define o que não versionar |
| `ide-compilers.iml` | Configuração do módulo IntelliJ |
| `docs/` | Toda a documentação do projeto |

---

## 🚫 O que NÃO versionar (.gitignore)

```gitignore
# Compilados
out/
*.class

# IDE
.idea/
*.iml
.kotlin

# Sistemas operacionais
.DS_Store
Thumbs.db

# Logs
*.log
```

---

## ✅ Checklist de Criação de Novos Componentes

Ao criar uma nova classe:

1. [ ] Está no pacote correto?
2. [ ] Segue a convenção de nomenclatura?
3. [ ] Tem uma responsabilidade única?
4. [ ] Suas dependências estão corretas (não cria ciclos)?
5. [ ] Está documentada (JavaDoc)?
6. [ ] Tem testes (quando aplicável)?

---

## 📊 Estatísticas do Projeto

**Total estimado de classes:** ~25-30
- IDE: ~4 classes
- Compiler: ~5 classes
- GALS: ~10 classes (geradas)
- Adapter: ~2 classes
- Error: ~5 classes
- Factory: ~2 classes
- Model: ~2 classes

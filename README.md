# IDE Compilador - Trabalho T2

**Disciplina:** Compiladores  
**Trabalho:** T2 - IDE e Analisador Sintático  
**Desenvolvido por:** Jordan Lippert e André Melo  
**Data de Apresentação:** 14/04/2026

## 📋 Descrição

IDE (Integrated Development Environment) integrada a um compilador que realiza análise léxica e sintática de uma linguagem de programação customizada, desenvolvida utilizando a ferramenta GALS.

## ✨ Funcionalidades Implementadas

### IDE
- ✅ Editor de código com fonte tamanho 14
- ✅ Console de mensagens com fonte tamanho 14
- ✅ Botão de compilação (F9)
- ✅ Menu completo (Arquivo, Compilar, Ajuda)
- ✅ Abrir/Salvar arquivos
- ✅ Syntax highlighting preparado (fonte monoespaçada)

### Análise Léxica (Base)
- ✅ Comentários de linha (`//`)
- ✅ Comentários de bloco (`/* */`)
- ✅ Identificadores e variáveis
- ✅ Literais numéricos (decimal, binário, hexadecimal, float)
- ✅ Literais de string e caractere
- ✅ Operadores aritméticos (`+`, `-`, `*`, `/`, `**`)
- ✅ Operadores relacionais (`==`, `!=`, `>`, `<`, `>=`, `<=`)
- ✅ Operadores lógicos (`&&`, `||`, `!`)
- ✅ Operadores bitwise (`&`, `|`, `^`, `~`, `<<`, `>>`)

### Análise Sintática (Base)
- ✅ Expressões aritméticas com precedência
- ✅ Expressões relacionais
- ✅ Expressões lógicas
- ✅ Expressões bitwise
- ✅ Atribuições simples
- ✅ Comandos `print()` e `log()`

### 🔄 A Implementar (T2)
- [ ] Declaração de variáveis múltiplas (1.5 pts)
- [ ] Declaração de vetores (1.5 pts)
- [ ] Desvio condicional simples - if (0.5 pts)
- [ ] Desvio condicional composto - if/else (1 pt)
- [ ] Laço while (2 pts)
- [ ] Laço for (2 pts)
- [ ] Laço do-while (2 pts)
- [ ] Entrada de dados - read() (0.5 pts)
- [ ] Saída expandida (0.5 pts)
- [ ] Atribuições com vetores (1.5 pts)
- [ ] Funções e procedimentos (1.5 pts)

## 🏗️ Arquitetura

O projeto utiliza padrões de design para garantir flexibilidade e manutenibilidade:

- **Strategy Pattern:** Fases de compilação (Léxica, Sintática, Semântica)
- **Adapter Pattern:** Isolamento das classes geradas pelo GALS
- **Chain of Responsibility:** Sistema de tratamento de erros
- **Factory Pattern:** Criação de componentes da IDE e parsers

### Estrutura de Pacotes

```
src/
├── Main.java                       # Entry point
└── br/compiler/
    ├── ide/                        # Interface gráfica
    │   ├── CompilerIDE.java
    │   ├── EditorPanel.java
    │   ├── ConsolePanel.java
    │   └── MenuBar.java
    ├── compiler/                   # Engine de compilação
    │   ├── CompilationEngine.java
    │   ├── CompilationPhase.java
    │   ├── LexicalPhase.java
    │   ├── SyntacticPhase.java
    │   └── SemanticPhase.java
    ├── adapter/                    # Adapter pattern
    │   ├── GalsAdapter.java
    │   └── GalsParserAdapter.java
    ├── error/                      # Sistema de erros
    │   ├── ErrorHandler.java
    │   ├── ErrorFormatter.java
    │   ├── LexicalErrorHandler.java
    │   ├── SyntacticErrorHandler.java
    │   └── SemanticErrorHandler.java
    ├── factory/                    # Factories
    │   ├── ComponentFactory.java
    │   └── ParserFactory.java
    ├── model/                      # Modelos de dados
    │   ├── CompilationResult.java
    │   ├── ErrorMessage.java
    │   └── ErrorSeverity.java
    └── gals/                       # Classes geradas (GALS)
        └── (a serem geradas)
```

## 🚀 Como Executar

### Pré-requisitos
- Java JDK 11 ou superior
- IntelliJ IDEA (recomendado)
- GALS (para regenerar as classes do parser)

### Passos

1. **Abra o projeto no IntelliJ IDEA**
   ```
   File → Open → Selecione a pasta ide-compilers
   ```

2. **Compile o projeto**
   ```
   Build → Build Project
   ```

3. **Execute a IDE**
   ```
   Run → Run 'Main'
   ```
   ou pressione **Shift + F10**

4. **Use a IDE**
   - Digite código no editor
   - Pressione **F9** ou clique em "Compilar"
   - Veja os resultados no console

## 📂 Exemplos de Teste

A pasta `resources/examples/` contém exemplos prontos para teste:

- `01_hello_world.txt` - Teste básico de print
- `02_arithmetic.txt` - Operações aritméticas
- `03_relational_logical.txt` - Operadores relacionais e lógicos
- `04_bitwise.txt` - Operadores bitwise
- `05_number_types.txt` - Diferentes tipos de números
- `06_strings.txt` - Strings e caracteres
- `07_comments.txt` - Comentários de linha e bloco
- `08_complete_example.txt` - Programa completo

**Para usar:** Menu Arquivo → Abrir → Selecione um exemplo

## 📚 Documentação

Documentação completa em `/docs`:

- [README.md](docs/README.md) - Índice da documentação
- [ARCHITECTURE.md](docs/ARCHITECTURE.md) - Arquitetura do sistema
- [DESIGN_PATTERNS.md](docs/DESIGN_PATTERNS.md) - Padrões de design
- [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) - Estrutura do projeto
- [GRAMMAR.md](docs/GRAMMAR.md) - Gramática GALS
- [DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) - Guia de desenvolvimento

## 🔧 Tecnologias

- **Linguagem:** Java
- **GUI:** Java Swing
- **Parser Generator:** GALS
- **IDE:** IntelliJ IDEA
- **Padrões de Design:** Strategy, Adapter, Chain of Responsibility, Factory

## 👥 Equipe

- Jordan Lippert
- André Melo

## 📝 Status do Projeto

**Fase Atual:** Implementação Base Completa

✅ Arquitetura implementada  
✅ IDE funcional  
✅ Sistema de compilação (Strategy)  
✅ Sistema de erros (Chain of Responsibility)  
✅ Exemplos de teste criados  
🔄 Aguardando geração de classes GALS  
🔄 Expansão da gramática para T2  

## 📅 Próximos Passos

1. Aguardar ajuste de comentário de bloco do colega
2. Expandir gramática GALS com estruturas do T2
3. Gerar classes Java pelo GALS
4. Integrar classes GALS com o adapter
5. Testar todas as funcionalidades
6. Preparar apresentação

---

**Projeto Acadêmico - Disciplina de Compiladores**  
**Semestre:** 2026/1  
**Data de Entrega:** 14/04/2026

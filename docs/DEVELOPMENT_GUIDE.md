# Guia de Desenvolvimento

## 🚀 Primeiros Passos

### Pré-requisitos

- **Java JDK:** 11 ou superior
- **IDE:** IntelliJ IDEA (recomendado)
- **GALS:** Ferramenta instalada e configurada
- **Git:** Para controle de versão

### Configuração do Ambiente

1. **Clone o repositório (se aplicável):**
   ```bash
   git clone <url-do-repositorio>
   cd ide-compilers
   ```

2. **Abra no IntelliJ IDEA:**
   - File → Open → Selecione a pasta `ide-compilers`

3. **Verifique o JDK:**
   - File → Project Structure → Project
   - Certifique-se de que o SDK está configurado (Java 11+)

4. **Estrutura de pastas:**
   - `src/` deve estar marcada como "Sources Root" (pasta azul)
   - `resources/` deve estar marcada como "Resources Root" (se criada)

---

## 📝 Fluxo de Trabalho

### 1. Trabalhar com a Gramática GALS

#### Editar a Gramática

1. Abra `compiler_definition.gals` no GALS
2. Faça as modificações necessárias (adicionar tokens, regras)
3. Salve o arquivo

#### Gerar Classes Java

1. No GALS: **Options → Generate**
2. Selecione o diretório: `src/main/compiler/gals/`
3. Gere as classes
4. **Importante:** As classes geradas **sobrescrevem** as antigas

#### Verificar Integração

Após gerar, verifique se não há erros de compilação no IntelliJ.

**Atenção:** Se a interface das classes GALS mudou, pode ser necessário ajustar o `GalsParserAdapter`.

#### Estratégia para mensagens de erro (recomendada)

- Trate `ParserConstants.PARSER_ERROR` como **mensagem base/fallback**.
- Centralize a mensagem final no `GalsParserAdapter` com contexto de depuração:
  - linha e coluna
  - token encontrado
  - lista de tokens esperados
- Ao regenerar pelo GALS, revise rapidamente:
  1. `ParserConstants.PARSER_ERROR`
  2. `GalsParserAdapter` (extração de tokens esperados e formatação final)

---

### 2. Implementar Novas Funcionalidades

#### Passo a Passo

1. **Identifique o requisito** (ex: implementar `if/else`)

2. **Atualize a gramática GALS:**
   - Adicione tokens necessários
   - Adicione regras gramaticais
   - Gere as classes

3. **Implemente na IDE (se necessário):**
   - Componentes visuais em `compiler.ide`
   - Lógica de negócio em `compiler.compiler`

4. **Crie testes:**
   - Arquivo de exemplo em `resources/examples/`
   - Teste manualmente na IDE

5. **Documente:**
   - Atualize `docs/GRAMMAR.md` se mudou a gramática
   - Atualize `docs/ARCHITECTURE.md` se mudou a estrutura

6. **Commit:**
   ```bash
   git add .
   git commit -m "feat: implementa estrutura if/else"
   ```

---

### 3. Padrão de Commits

Siga o padrão **Conventional Commits:**

- `feat:` Nova funcionalidade
- `fix:` Correção de bug
- `docs:` Documentação
- `refactor:` Refatoração sem mudança de comportamento
- `test:` Adicionar ou corrigir testes
- `style:` Formatação, ponto e vírgula, etc.
- `chore:` Atualizações de build, dependências, etc.

**Exemplos:**
```bash
git commit -m "feat: adiciona suporte a declaração de vetores"
git commit -m "fix: corrige precedência de operadores bitwise"
git commit -m "docs: atualiza diagrama de arquitetura"
git commit -m "refactor: extrai lógica de formatação de erros"
```

---

## 🏗️ Como Implementar Cada Componente

### IDE Components (`compiler.ide`)

#### CompilerIDE.java (Janela Principal)

```java
public class CompilerIDE extends JFrame {
    private EditorPanel editor;
    private ConsolePanel console;
    private CompilationEngine engine;
    
    public CompilerIDE() {
        initComponents();
        setupLayout();
        setupListeners();
    }
    
    private void onCompileClick() {
        String sourceCode = editor.getText();
        CompilationResult result = engine.compile(sourceCode);
        console.display(result);
    }
}
```

**Responsabilidades:**
- Criar e organizar componentes (editor, console, botões)
- Escutar eventos (clique em Compilar)
- Delegar compilação para `CompilationEngine`

---

#### EditorPanel.java (Área de Edição)

```java
public class EditorPanel extends JPanel {
    private JTextPane textPane;
    
    public EditorPanel() {
        textPane = ComponentFactory.createEditor();
        setupLineNumbers(); // Opcional
        add(new JScrollPane(textPane));
    }
    
    public String getText() {
        return textPane.getText();
    }
    
    public void setText(String text) {
        textPane.setText(text);
    }
}
```

**Responsabilidades:**
- Exibir área de texto com formatação
- Fornecer métodos para get/set texto
- (Opcional) Numeração de linhas, syntax highlighting

---

#### ConsolePanel.java (Console de Mensagens)

```java
public class ConsolePanel extends JPanel {
    private JTextArea textArea;
    
    public ConsolePanel() {
        textArea = ComponentFactory.createConsole();
        add(new JScrollPane(textArea));
    }
    
    public void display(CompilationResult result) {
        if (result.isSuccess()) {
            appendSuccess("✓ Compilação concluída com sucesso!");
        } else {
            appendError(result.getFormattedError());
        }
    }
    
    public void clear() {
        textArea.setText("");
    }
    
    private void appendSuccess(String msg) {
        textArea.append("[INFO] " + msg + "\n");
    }
    
    private void appendError(String msg) {
        textArea.append("[ERRO] " + msg + "\n");
    }
}
```

**Responsabilidades:**
- Exibir mensagens de sucesso/erro
- Formatar mensagens com cores (opcional)
- Limpar console

---

### Compilation Engine (`compiler.compiler`)

#### CompilationEngine.java

```java
public class CompilationEngine {
    private List<CompilationPhase> phases = new ArrayList<>();
    private ErrorHandler errorHandler;
    
    public CompilationEngine() {
        setupPhases();
        setupErrorHandler();
    }
    
    private void setupPhases() {
        GalsAdapter adapter = ParserFactory.createGalsAdapter();
        phases.add(new LexicalPhase(adapter));
        phases.add(new SyntacticPhase(adapter));
    }
    
    private void setupErrorHandler() {
        ErrorHandler lexical = new LexicalErrorHandler();
        ErrorHandler syntactic = new SyntacticErrorHandler();
        ErrorHandler formatter = new ErrorFormatter();
        
        lexical.setNext(syntactic);
        syntactic.setNext(formatter);
        
        this.errorHandler = lexical;
    }
    
    public CompilationResult compile(String sourceCode) {
        for (CompilationPhase phase : phases) {
            try {
                CompilationResult result = phase.execute(sourceCode);
                
                if (!result.isSuccess()) {
                    ErrorMessage error = errorHandler.handle(result);
                    return CompilationResult.error(error);
                }
                
            } catch (Exception e) {
                return CompilationResult.exception(e);
            }
        }
        
        return CompilationResult.success();
    }
}
```

---

### Adapter (`compiler.adapter`)

#### GalsParserAdapter.java

```java
public class GalsParserAdapter implements GalsAdapter {
    
    @Override
    public CompilationResult performLexicalAnalysis(String sourceCode) {
        try {
            Lexico lexico = new Lexico(sourceCode);
            Token token;
            
            while ((token = lexico.nextToken()) != null) {
                // Apenas verifica se não há erros léxicos
            }
            
            return CompilationResult.success();
            
        } catch (LexicalError e) {
            return CompilationResult.error(
                "Léxico",
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
            Semantico semantico = new Semantico();
            
            sintatico.parse(lexico, semantico);
            
            return CompilationResult.success();
            
        } catch (LexicalError e) {
            return CompilationResult.error("Léxico", e.getMessage(), e.getPosition());
        } catch (SyntaticError e) {
            return CompilationResult.error("Sintático", e.getMessage(), e.getPosition());
        } catch (SemanticError e) {
            return CompilationResult.error("Semântico", e.getMessage(), e.getPosition());
        }
    }
}
```

---

## 🧪 Testes

### Criar Exemplos de Teste

Crie arquivos em `resources/examples/` para testar cada funcionalidade:

**01_hello_world.txt:**
```javascript
print("Hello, World!");
```

**02_variables.txt:**
```javascript
int x, y, z;
x = 10;
y = 20;
z = x + y;
print(z);
```

**03_conditionals.txt:**
```javascript
int x;
x = 15;

if (x > 10) {
    print("maior que 10");
}

if (x > 20) {
    print("maior que 20");
} else {
    print("menor ou igual a 20");
}
```

### Testar Manualmente

1. Abra a IDE
2. Carregue o exemplo (ou digite)
3. Clique em "Compilar"
4. Verifique o console:
   - ✓ Deve mostrar sucesso se o código está correto
   - ✗ Deve mostrar erro específico se há problema

---

## 🐛 Debug

### Debugar Gramática GALS

1. No GALS, use **Simulate** para testar a gramática
2. Digite um código de exemplo
3. Veja se é aceito ou rejeitado
4. Ajuste as regras conforme necessário

### Debugar Java (IntelliJ)

1. Coloque breakpoints nas classes:
   - `CompilationEngine.compile()`
   - `GalsParserAdapter.performSyntacticAnalysis()`
   - `ErrorHandler.handle()`

2. Execute em modo Debug (Shift + F9)

3. Inspecione variáveis:
   - `sourceCode`
   - `result`
   - `token`

---

## 📚 Recursos Úteis

### Documentação

- [Manual do GALS](http://gals.sourceforge.net/)
- [Java Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Conventional Commits](https://www.conventionalcommits.org/)

### Dicas

**Erros comuns no GALS:**
- Conflitos shift/reduce: Revisar precedência
- Tokens não reconhecidos: Verificar expressões regulares
- Gramática ambígua: Refatorar regras

**Erros comuns no Java:**
- `NullPointerException`: Inicializar objetos corretamente
- `ClassNotFoundException`: Verificar pacotes e imports
- Fonte não aparece tamanho 14: Verificar `setFont()`

---

## ✅ Checklist de Entrega

Antes de considerar o trabalho pronto:

### Funcionalidades
- [ ] IDE com editor (fonte 14)
- [ ] IDE com console (fonte 14)
- [ ] Botão "Compilar" funcional
- [ ] Declaração de variáveis múltiplas
- [ ] Declaração de vetores
- [ ] if simples
- [ ] if/else
- [ ] while
- [ ] for
- [ ] do-while
- [ ] read()
- [ ] print() expandido
- [ ] Atribuições com expressões
- [ ] Funções e procedimentos
- [ ] Chamadas de função

### Qualidade
- [ ] Código comentado (onde necessário)
- [ ] Sem warnings de compilação
- [ ] Padrões de design implementados
- [ ] Exemplos de teste criados
- [ ] README atualizado

### Documentação
- [ ] docs/ completo
- [ ] Comentários JavaDoc nas classes principais
- [ ] Exemplos documentados

### Apresentação
- [ ] Preparar demonstração
- [ ] Testar todos os exemplos
- [ ] Preparar explicação da arquitetura

---

## 👥 Colaboração

### Workflow com Colega

1. **Branches separados:**
   ```bash
   git checkout -b feature/comentario-bloco  # Colega
   git checkout -b feature/ide-interface     # Você
   ```

2. **Commits regulares:**
   ```bash
   git add .
   git commit -m "feat: adiciona componente X"
   git push origin feature/ide-interface
   ```

3. **Merge quando pronto:**
   ```bash
   git checkout master
   git merge feature/ide-interface
   git merge feature/comentario-bloco
   ```

### Comunicação

- Usar Issues/Cards para tarefas
- Documentar decisões importantes em `docs/`
- Avisar antes de modificar `trabalho_compiladores_01.gals`

---

**Boa sorte no desenvolvimento! 🚀**

**Dúvidas?** Consulte a documentação em `docs/` ou peça ajuda ao colega.

# 🚀 Guia Rápido - Próximos Passos

## ✅ O que já está implementado

1. **Arquitetura completa** com padrões de design
2. **IDE funcional** (editor + console + menu)
3. **Sistema de compilação** pronto para receber classes GALS
4. **Sistema de erros** com formatação automática
5. **8 exemplos de teste** prontos
6. **Documentação completa** em `/docs`

## 🔧 Próximos Passos para Completar o T2

### 1. Aguardar Classes GALS

Seu colega está ajustando o comentário de bloco no arquivo `trabalho_compiladores_01.gals`.

Quando ele enviar:
- Substitua o arquivo `trabalho_compiladores_01.gals`
- Ou aplique o patch que ele enviar

### 2. Expandir a Gramática GALS

Você precisa adicionar as seguintes estruturas ao arquivo `.gals`:

**Tokens novos:**
```gals
DO: do
READ: read
RETURN: return
VOID: void
```

**Não-terminais novos:**
```gals
<DECLARATION>
<VAR_LIST>
<TYPE>
<IF_STATEMENT>
<WHILE_STATEMENT>
<FOR_STATEMENT>
<DO_WHILE_STATEMENT>
<READ_STATEMENT>
<FUNCTION_DECLARATION>
<FUNCTION_CALL>
etc.
```

**Consulte:** `docs/GRAMMAR.md` para a gramática completa proposta

### 3. Gerar Classes Java com GALS

1. Abra `trabalho_compiladores_01.gals` no GALS
2. Menu: **Options → Generate**
3. Diretório de saída: `src/br/compiler/gals/`
4. Gere as classes

Isso criará:
- `Lexico.java`
- `Sintatico.java`
- `Semantico.java`
- `Token.java`
- `LexicalError.java`
- `SyntaticError.java`
- `SemanticError.java`
- `Constants.java`
- `ParserConstants.java`
- `ScannerConstants.java`

### 4. Atualizar o GalsParserAdapter

Arquivo: `src/br/compiler/adapter/GalsParserAdapter.java`

Substitua o código TODO pelos métodos reais usando as classes GALS:

```java
@Override
public CompilationResult performLexicalAnalysis(String sourceCode) {
    try {
        Lexico lexico = new Lexico(sourceCode);
        Token token;
        List<Object> tokens = new ArrayList<>();
        
        while ((token = lexico.nextToken()) != null) {
            tokens.add(token);
        }
        
        this.tokens = tokens;
        return CompilationResult.success(tokens);
        
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
```

### 5. Testar a IDE

1. Execute `Main.java`
2. A IDE abrirá
3. Abra um exemplo: `Arquivo → Abrir → resources/examples/01_hello_world.txt`
4. Clique em **Compilar** (ou F9)
5. Veja o resultado no console

### 6. Criar Novos Exemplos para T2

Quando implementar as novas estruturas, crie exemplos:

```
resources/examples/
├── 09_variables_declaration.txt     # int x, y, z;
├── 10_arrays.txt                     # int v[10];
├── 11_if_simple.txt                  # if (x > 10) { }
├── 12_if_else.txt                    # if/else
├── 13_while.txt                      # while
├── 14_for.txt                        # for
├── 15_do_while.txt                   # do-while
├── 16_read.txt                       # read(x);
├── 17_functions.txt                  # function declarations
└── 18_complete_program.txt           # Programa completo usando tudo
```

## 🧪 Como Testar

### Teste Manual

1. Execute a IDE
2. Digite código no editor
3. Clique em Compilar
4. Verifique mensagens no console

### Teste com Exemplos

1. Menu: Arquivo → Abrir
2. Selecione um arquivo de `resources/examples/`
3. Clique em Compilar
4. Deve mostrar "✓ Compilação concluída com sucesso!"

### Teste de Erros

Digite código com erro proposital:
```
x = ;  // Erro sintático: expressão incompleta
```

Console deve mostrar:
```
[ERRO] Sintático - Linha X: ...
```

## 📋 Checklist Final

Antes da apresentação:

- [ ] Gramática GALS expandida com todas as estruturas do T2
- [ ] Classes GALS geradas
- [ ] GalsParserAdapter integrado com classes GALS
- [ ] Todos os exemplos testados e funcionando
- [ ] Console mostra erros formatados corretamente
- [ ] IDE abre/salva arquivos corretamente
- [ ] Fonte tamanho 14 no editor e console
- [ ] README.md atualizado com nomes da equipe
- [ ] Preparar demonstração ao vivo

## 🎯 Demonstração Sugerida

1. **Mostrar a IDE:**
   - Interface limpa
   - Editor com fonte 14
   - Console com fonte 14

2. **Compilar código válido:**
   - Abrir exemplo completo
   - Compilar com sucesso
   - Mostrar mensagem de sucesso

3. **Mostrar erro:**
   - Digite código com erro
   - Compilar
   - Mostrar erro formatado no console

4. **Explicar arquitetura:**
   - Mostrar diagrama (docs/ARCHITECTURE.md)
   - Explicar padrões de design
   - Mostrar código (Strategy, Adapter, Chain)

5. **Mostrar gramática:**
   - Abrir GALS
   - Mostrar a gramática
   - Explicar as estruturas implementadas

## 📞 Dúvidas?

Consulte a documentação em `/docs`:
- `GRAMMAR.md` - Para expandir a gramática
- `DEVELOPMENT_GUIDE.md` - Para implementar
- `ARCHITECTURE.md` - Para entender a estrutura

## 🎉 Boa Sorte!

A base está sólida. Agora é só:
1. Expandir a gramática
2. Gerar as classes GALS
3. Integrar
4. Testar
5. Apresentar!

**Você consegue! 🚀**

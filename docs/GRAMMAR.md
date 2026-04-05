# Gramática GALS - Documentação

## 📝 Sobre a Linguagem

A linguagem definida neste projeto é uma linguagem de programação imperativa simplificada, com sintaxe híbrida inspirada em JavaScript/C.

## 🎯 Status Atual

### ✅ Implementado (Base)

- **Tokens:**
  - Comentários de linha (`//`)
  - Comentários de bloco (`/* */`) - **ajuste pendente do colega**
  - Palavras-reservadas básicas
  - Identificadores (variáveis)
  - Literais numéricos (decimal, binário, hexadecimal, float)
  - Literais de caractere e string
  - Operadores aritméticos (`+`, `-`, `*`, `/`, `**`)
  - Operadores relacionais (`==`, `!=`, `>`, `<`, `>=`, `<=`)
  - Operadores lógicos (`&&`, `||`, `!`)
  - Operadores bitwise (`&`, `|`, `^`, `~`, `<<`, `>>`)
  - Delimitadores (`()`, `[]`, `{}`, `,`, `:`, `.`, `;`)

- **Gramática:**
  - Expressões aritméticas com precedência
  - Expressões relacionais
  - Expressões lógicas
  - Expressões bitwise
  - Parênteses para agrupamento
  - Atribuição simples
  - Comandos `print()` e `log()`

### 🔄 A Implementar (T2)

#### 1. Declaração de Variáveis e Vetores (1.5 pontos)

**Requisito:** Suporte a múltiplos nomes (2 ou mais variáveis/vetores na mesma declaração)

**Sintaxe proposta:**
```javascript
// Variáveis simples
int x;
int a, b, c;
float pi, e;

// Vetores
int vetor[10];
int v1[5], v2[10], v3[20];
```

**Não-terminais necessários:**
- `<DECLARATION>`
- `<VAR_LIST>`
- `<TYPE>`

---

#### 2. Desvio Condicional Simples (0.5 pontos)

**Requisito:** if sem else

**Sintaxe proposta:**
```javascript
if (x > 10) {
    print(x);
}
```

**Não-terminais necessários:**
- `<IF_STATEMENT>`
- `<CONDITION>`
- `<BLOCK>`

---

#### 3. Desvio Condicional Composto (1 ponto)

**Requisito:** if com else

**Sintaxe proposta:**
```javascript
if (x > 10) {
    print("maior");
} else {
    print("menor ou igual");
}

// Bonus: else if
if (x > 10) {
    print("maior que 10");
} else if (x > 5) {
    print("entre 6 e 10");
} else {
    print("menor ou igual a 5");
}
```

**Não-terminais necessários:**
- `<IF_ELSE_STATEMENT>`
- `<ELSE_CLAUSE>` (opcional)

---

#### 4. Laços de Repetição (2 pontos)

**Requisito:** Três tipos de laços

##### a) Pré-testado (while)
```javascript
while (x < 10) {
    x = x + 1;
    print(x);
}
```

##### b) Pré-testado com variável de controle (for)
```javascript
for (i = 0; i < 10; i = i + 1) {
    print(i);
}
```

##### c) Pós-testado (do-while)
```javascript
do {
    print(x);
    x = x - 1;
} while (x > 0);
```

**Não-terminais necessários:**
- `<WHILE_STATEMENT>`
- `<FOR_STATEMENT>`
- `<DO_WHILE_STATEMENT>`
- `<FOR_INIT>`
- `<FOR_CONDITION>`
- `<FOR_INCREMENT>`

---

#### 5. Entrada de Dados (0.5 pontos)

**Requisito:** Leitura de variáveis e vetores

**Sintaxe proposta:**
```javascript
read(x);
read(nome);
read(vetor[i]);
```

**Não-terminais necessários:**
- `<READ_STATEMENT>`
- `<VARIABLE_REF>` (para suportar `var` e `var[index]`)

**Token novo:**
- `READ: read`

---

#### 6. Saída de Dados (0.5 pontos)

**Requisito:** Escrever variáveis, vetores e literais

**Status:** Parcialmente implementado (já existe `print` e `log`)

**Expansão necessária:**
```javascript
print(x);
print(vetor[i]);
print("Resultado: ", x);
print(1 + 2 + 3);
```

**Ajuste:** Permitir múltiplos argumentos no `print()`

---

#### 7. Atribuição com Expressões (1.5 pontos)

**Requisito:** Variáveis/vetores recebendo expressões com variáveis, vetores, literais e chamadas de funções

**Status:** Parcialmente implementado (atribuição básica existe)

**Expansão necessária:**
```javascript
x = 10;
y = x + 5;
z = soma(x, y);
vetor[i] = x * 2;
vetor[i] = vetor[i] + 1;
matriz[i][j] = soma(i, j);
```

**Ajuste:** Suporte a vetores no lado esquerdo e chamadas de função no lado direito

---

#### 8. Sub-rotinas (1.5 pontos)

**Requisito:** Procedimentos e funções com parâmetros

**Sintaxe proposta:**
```javascript
// Função (retorna valor)
function int soma(int a, int b) {
    return a + b;
}

// Procedimento (não retorna valor - void)
function void imprime(int x) {
    print(x);
}

// Chamada
resultado = soma(10, 20);
imprime(resultado);
```

**Não-terminais necessários:**
- `<FUNCTION_DECLARATION>`
- `<PARAMETER_LIST>`
- `<PARAMETER>`
- `<RETURN_STATEMENT>`
- `<FUNCTION_CALL>`
- `<ARGUMENT_LIST>`

**Tokens novos:**
- `RETURN: return`
- `VOID: void` (se ainda não existir)

---

## 📋 Estrutura Proposta da Gramática (T2)

```gals
#NonTerminals
<PROGRAM>
<DECLARATION>
<VAR_LIST>
<TYPE>
<STATEMENT>
<BLOCK>
<IF_STATEMENT>
<IF_ELSE_STATEMENT>
<ELSE_CLAUSE>
<WHILE_STATEMENT>
<FOR_STATEMENT>
<DO_WHILE_STATEMENT>
<READ_STATEMENT>
<WRITE_STATEMENT>
<ASSIGNMENT>
<FUNCTION_DECLARATION>
<PARAMETER_LIST>
<PARAMETER>
<RETURN_STATEMENT>
<FUNCTION_CALL>
<ARGUMENT_LIST>
<VARIABLE_REF>
<EXPRESSION>
<CONDITION>
... (expressões já existentes)

#Grammar
<PROGRAM> ::= <DECLARATION> <PROGRAM>
            | <FUNCTION_DECLARATION> <PROGRAM>
            | <STATEMENT> <PROGRAM>
            | ε ;

<DECLARATION> ::= <TYPE> <VAR_LIST> EOL ;

<VAR_LIST> ::= VARIABLE
             | VARIABLE OPEN_BRACKET NUMBER CLOSE_BRACKET
             | <VAR_LIST> COMMA VARIABLE
             | <VAR_LIST> COMMA VARIABLE OPEN_BRACKET NUMBER CLOSE_BRACKET ;

<TYPE> ::= INT | FLOAT | STRING ;

<STATEMENT> ::= <ASSIGNMENT>
              | <IF_STATEMENT>
              | <IF_ELSE_STATEMENT>
              | <WHILE_STATEMENT>
              | <FOR_STATEMENT>
              | <DO_WHILE_STATEMENT>
              | <READ_STATEMENT>
              | <WRITE_STATEMENT>
              | <RETURN_STATEMENT>
              | <FUNCTION_CALL> EOL
              | <BLOCK> ;

<BLOCK> ::= OPEN_BRACE <STATEMENT>+ CLOSE_BRACE ;

<IF_STATEMENT> ::= IF OPEN_PARENTHESES <CONDITION> CLOSE_PARENTHESES <BLOCK> ;

<IF_ELSE_STATEMENT> ::= IF OPEN_PARENTHESES <CONDITION> CLOSE_PARENTHESES <BLOCK> 
                        ELSE <BLOCK> ;

<WHILE_STATEMENT> ::= WHILE OPEN_PARENTHESES <CONDITION> CLOSE_PARENTHESES <BLOCK> ;

<FOR_STATEMENT> ::= FOR OPEN_PARENTHESES <ASSIGNMENT> EOL <CONDITION> EOL <ASSIGNMENT> 
                    CLOSE_PARENTHESES <BLOCK> ;

<DO_WHILE_STATEMENT> ::= DO <BLOCK> WHILE OPEN_PARENTHESES <CONDITION> CLOSE_PARENTHESES EOL ;

<READ_STATEMENT> ::= READ OPEN_PARENTHESES <VARIABLE_REF> CLOSE_PARENTHESES EOL ;

<WRITE_STATEMENT> ::= PRINT OPEN_PARENTHESES <ARGUMENT_LIST> CLOSE_PARENTHESES EOL ;

<ASSIGNMENT> ::= <VARIABLE_REF> EQUALS <EXPRESSION> ;

<FUNCTION_DECLARATION> ::= FUNCTION <TYPE> VARIABLE OPEN_PARENTHESES <PARAMETER_LIST> 
                           CLOSE_PARENTHESES <BLOCK> ;

<PARAMETER_LIST> ::= <PARAMETER>
                   | <PARAMETER> COMMA <PARAMETER_LIST>
                   | ε ;

<PARAMETER> ::= <TYPE> VARIABLE
              | <TYPE> VARIABLE OPEN_BRACKET CLOSE_BRACKET ;

<RETURN_STATEMENT> ::= RETURN <EXPRESSION> EOL ;

<FUNCTION_CALL> ::= VARIABLE OPEN_PARENTHESES <ARGUMENT_LIST> CLOSE_PARENTHESES ;

<ARGUMENT_LIST> ::= <EXPRESSION>
                  | <EXPRESSION> COMMA <ARGUMENT_LIST>
                  | ε ;

<VARIABLE_REF> ::= VARIABLE
                 | VARIABLE OPEN_BRACKET <EXPRESSION> CLOSE_BRACKET ;

<CONDITION> ::= <EXPRESSION> ;

... (manter expressões já existentes)
```

## 🔧 Tokens Adicionais Necessários

```gals
// Já existem (conferir):
IF: if
ELSE: else
WHILE: while
FOR: for
FUNCTION: function
INT: int
FLOAT: float
STRING: string

// Adicionar:
DO: do
READ: read
RETURN: return
VOID: void
```

## 📊 Checklist de Implementação

- [ ] Adicionar tokens faltantes (DO, READ, RETURN, VOID)
- [ ] Implementar declarações de variáveis
- [ ] Implementar if simples
- [ ] Implementar if/else
- [ ] Implementar while
- [ ] Implementar for
- [ ] Implementar do-while
- [ ] Implementar read()
- [ ] Expandir print() para múltiplos argumentos
- [ ] Implementar suporte a vetores em atribuições
- [ ] Implementar declaração de funções
- [ ] Implementar chamada de funções
- [ ] Implementar return
- [ ] Testar todas as estruturas
- [ ] Ajustar comentário de bloco (aguardando colega)

## 🧪 Exemplos de Teste

Criar arquivos em `resources/examples/` cobrindo cada funcionalidade.

---

**Última atualização:** 05/04/2026  
**Responsável:** Equipe de desenvolvimento

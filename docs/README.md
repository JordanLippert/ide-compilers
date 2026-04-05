# Documentação do Projeto - IDE e Analisador Sintático

## 📋 Informações do Projeto

- **Disciplina:** Compiladores
- **Trabalho:** T2 - IDE e Analisador Sintático
- **Data de Apresentação:** 14/04/2026
- **Desenvolvido por:** Jordan Lippert e André Melo

## 📚 Índice da Documentação

1. [Arquitetura do Sistema](ARCHITECTURE.md) - Visão geral da arquitetura e componentes
2. [Padrões de Design](DESIGN_PATTERNS.md) - Padrões utilizados e justificativas
3. [Estrutura do Projeto](PROJECT_STRUCTURE.md) - Organização de pacotes e classes
4. [Gramática GALS](GRAMMAR.md) - Definição da linguagem e gramática
5. [Guia de Desenvolvimento](DEVELOPMENT_GUIDE.md) - Como desenvolver e testar

## 🎯 Objetivo do Projeto

Desenvolver uma IDE (Integrated Development Environment) integrada a um compilador que realiza análise léxica e sintática de uma linguagem de programação customizada.

### Requisitos Principais

**IDE (1 ponto):**
- Editor de código com fonte tamanho 14
- Análise sintática (botão Compilar)
- Console de mensagens de erro/debug com fonte 14

**Analisador Sintático (usando GALS):**
- ✅ Expressões aritméticas, relacionais, lógicas e bitwise (já implementado no T1)
- 🔄 Declaração de variáveis e vetores múltiplos (1.5 pontos)
- 🔄 Desvio condicional simples - if (0.5 pontos)
- 🔄 Desvio condicional composto - if/else (1 ponto)
- 🔄 Três tipos de laços (2 pontos):
  - Pré-testado (while)
  - Pré-testado com controle (for)
  - Pós-testado (do-while)
- 🔄 Entrada de dados (0.5 pontos)
- 🔄 Saída de dados (0.5 pontos) - parcialmente implementado (print, log)
- 🔄 Atribuição com expressões (1.5 pontos) - parcialmente implementado
- 🔄 Sub-rotinas (funções e procedimentos) (1.5 pontos)

**Legenda:** ✅ Completo | 🔄 Em desenvolvimento | ❌ Não iniciado

## 🛠️ Tecnologias

- **Linguagem de Implementação:** Java
- **Ferramenta de Análise:** GALS (Gerador de Analisadores Léxicos e Sintáticos)
- **Interface Gráfica:** Java Swing
- **IDE de Desenvolvimento:** IntelliJ IDEA

## 👥 Equipe

- Jordan Lippert
- André Melo 

## 📝 Status Atual

**Fase:** Planejamento e Design
**Última Atualização:** 05/04/2026

### Progresso

- [x] Análise de requisitos
- [x] Definição de arquitetura
- [x] Definição de padrões de design
- [x] Estrutura de pacotes
- [ ] Implementação da IDE
- [ ] Expansão da gramática GALS
- [ ] Integração GALS + IDE
- [ ] Testes e validação

## 📞 Próximos Passos

1. Aguardar ajuste de comentário de bloco do colega
2. Expandir gramática com estruturas do T2
3. Gerar classes Java pelo GALS
4. Implementar IDE básica (editor + console)
5. Implementar engine de compilação
6. Integrar componentes
7. Criar exemplos de teste
8. Documentar casos de teste

package compiler.codegen;

import compiler.gals.Constants;
import compiler.gals.Symbol;
import compiler.gals.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BipCodeGenerator {
    private final static List<Integer> _relationalOPerationConstants = Arrays.asList(Constants.t_greater_than, Constants.t_less_than,Constants.t_greater_equal,Constants.t_less_equal,Constants.t_equality,Constants.t_inequality);


    private final List<Token> tokens;
    private final List<Symbol> symbolTable;
    private final List<String> dataLines = new ArrayList<>();
    private final List<String> codeLines = new ArrayList<>();
    private int pos = 0;
    private int _ifsCount = 1;
    private boolean _conditionalStatement = false;
    private String _conditionalFalseLabel;

    public BipCodeGenerator(List<Token> tokens, List<Symbol> symbolTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
    }

    public String generate() {
        // TODO: essa função deveria sometne gerar e retornar a string (ou linhas) com toda a seção de dados (.data)
        buildDataSection();
        // TODO: essa função deveria sometne gerar e retornar a string (ou linhas) com toda a seção de código (.text)
        buildCodeSection();
        // TODO: Essa função deveria receber como input a saída das duas funções anteriores e gerar o .asm completo
        return formatOutput();
    }

    private void buildDataSection() {
        // TODO: o certo é não utilizar a tabela de símbolos, ser independente dela. O certo seria pegar a inicialização de todas as váriaveis e guardar sometne as variavies com seus valores iniciais
        for (Symbol s : symbolTable) {
            if (Boolean.TRUE.equals(s.isFunction))
                continue;
            if (Boolean.TRUE.equals(s.isParameter))
                continue;
            if (Boolean.TRUE.equals(s.isArray)) {
                int size =
                        s.arraySize != null
                                ? s.arraySize
                                : 10;
                dataLines.add(s.id + ": [" + size + "]");
            }
            else {
                Object initialValue =
                        s.initialValue != null
                                ? s.initialValue
                                : 0;
                dataLines.add(
                        s.id + ": " + initialValue
                );
            }
        }

        // TODO: É errado utilizar __shift_tmp. Deveria ser desnecessário
        dataLines.add("__shift_tmp: 0");
        // TODO: Transformar os temps em constantes para serem reutilizadas no código. Ao invés de declarar elas aqui, podemos fix endereços especificos para sempre manterem valores temporários, retirando a necessidade de declarar váriaveis temporarias
        // No Bipide, utilizamos 1000 e 1001 para as variaveis temporarias
        dataLines.add("temp1: 0");
        dataLines.add("temp2: 0");
    }

    private void buildCodeSection() {
        pos = 0;
        while (pos < tokens.size()) {
            // TODO: Deveria alterar o código para passar um statement com entrada a função parseStatement(), sendo a saída o código equivalente em BIP
            parseStatement();
        }
        emit("HLT 0");
    }

    private void parseStatement() {
        Token t = peek();
        if (t == null) return;
        int id = t.getId();

        // TODO: Mudar para switch case
        if (isTypeKeyword(id) || id == Constants.t_void) {
            skipDeclaration();
            return;
        }
        if (id == Constants.t_read) { parseReadStatement(); return; }
        if (id == Constants.t_write) { parseWriteStatement(); return; }
        if (id == Constants.t_return) { skipUntilSemicolon(); return; }
        if (id == Constants.t_if) { parseConditionalStatement(); return; }
        if (id == Constants.t_variable && isAssignmentAhead()) { parseAssignmentStatement(); return; }
        advance();
    }

    // TODO: Está funcionando somente para váriaves e não para vetores. Deve ser analisado o motivo do erro. Possivelmente erro da gramatica
    // TODO: Esta função deveria receber uma entrada de texto com o código a ser analisdo, e retornar o código BIP equivalente
    private void parseAssignmentStatement() {
        String varName = peek().getLexeme();
        advance(); // variável

        // suporte futuro para vetor
        boolean isArray = false;

        if (peek() != null &&
                peek().getId() == Constants.t_open_bracket) {

            isArray = true;

            advance(); // [

            loadArrayIndex();

            if (peek() != null &&
                    peek().getId() == Constants.t_close_bracket) {
                advance(); // ]
            }
        }

        advance(); // '='

        Object expr = parseExpression();
        if (!(expr instanceof String &&
                expr.equals("__acc__"))) {

            loadOperand(expr);
        }

        if (isArray) {
            emit("STOV " + varName);
        } else {
            emit("STO " + varName);
        }

        if (peek() != null &&
                peek().getId() == Constants.t_semicolon) {
            advance();
        }
    }

    // TODO: Esta função deveria receber uma entrada de texto com o código a ser analisdo, e retornar o código BIP equivalente
    private void parseReadStatement() {
        advance(); // read
        if (peek() != null &&
                peek().getId() == Constants.t_open_parentheses) {
            advance(); // (
        }
        while (peek() != null &&
                peek().getId() != Constants.t_close_parentheses)
        {
            Token t = peek();

            if (t.getId() == Constants.t_variable)
            {
                String varName = t.getLexeme();
                advance();

                // read(vetor[indice])
                if (peek() != null &&
                        peek().getId() == Constants.t_open_bracket)
                {
                    advance(); // [

                    loadArrayIndex();

                    if (peek() != null &&
                            peek().getId() == Constants.t_close_bracket)
                    {
                        advance(); // ]
                    }

                    emit("LD $in_port");
                    emit("STOV " + varName);
                }
                else
                {
                    // read(variavel)
                    emit("LD $in_port");
                    emit("STO " + varName);
                }
            }
            else if (t.getId() == Constants.t_comma)
            {
                advance();
            }
            else
            {
                advance();
            }
        }

        if (peek() != null &&
                peek().getId() == Constants.t_close_parentheses)
        {
            advance(); // )
        }

        if (peek() != null &&
                peek().getId() == Constants.t_semicolon)
        {
            advance(); // ;
        }
    }

    // TODO: Esta função deveria receber uma entrada de texto com o código a ser analisdo, e retornar o código BIP equivalente
    private void parseConditionalStatement() {
        _conditionalStatement = true;

        int ifId = _ifsCount++;

        advance(); // if
        advance(); // (

        parseExpression();

        advance(); // )

        parseBlock();

        Token next = peek();

        boolean hasElse =
                next != null &&
                        next.getId() == Constants.t_else;

        if (hasElse) {
            String elseLabel = "ELSE" + ifId;
            String endLabel  = "FIMSE" + ifId;
            replaceLastFalseJumpTarget(elseLabel);
            emit("JMP " + endLabel);
            codeLines.add(elseLabel + ":");
            advance(); // else
            parseBlock();
            codeLines.add(endLabel + ":");
        }
        else {
            String endLabel = "FIMSE" + ifId;
            replaceLastFalseJumpTarget(endLabel);
            codeLines.add(endLabel + ":");
        }

        _conditionalStatement = false;
    }

    // TODO: Esta função deveria receber uma entrada de texto com o código a ser analisdo, e retornar o código BIP equivalente
    private void parseBlock() {
        if (peek() != null &&
                peek().getId() == Constants.t_open_brace) {
            advance();
        }

        while (peek() != null &&
                peek().getId() != Constants.t_close_brace) {
            parseStatement();
        }

        if (peek() != null &&
                peek().getId() == Constants.t_close_brace) {
            advance();
        }
    }

    // TODO: Esta função deveria receber uma entrada de texto com o código a ser analisdo, e retornar o código BIP equivalente
    private void parseWriteStatement() {
        advance(); // write
        advance(); // (

        while (peek() != null &&
                peek().getId() != Constants.t_close_parentheses) {

            parseExpression();
            emit("STO $out_port");

            if (peek() != null &&
                    peek().getId() == Constants.t_comma) {
                advance();
            }
        }

        if (peek() != null) advance(); // )
        if (peek() != null &&
                peek().getId() == Constants.t_semicolon) {
            advance();
        }
    }

    // TODO: Esta função deveria receber uma entrada de texto com o código a ser analisdo, e retornar o código BIP equivalente
    // Analisar se há alguma maneira de simplificar a resolução/redução de expressões
    private Object parseExpression() {

        Object left = parseSimplePrimary();

        if (left == null) {
            return null;
        }

        loadOperand(left);

        while (peek() != null) {
            // TODO: Melhorar isso para um switch case
            if (isBinaryOperator(peek().getId())) {

                Token opToken = advance();

                Object right = parseSimplePrimary();

                if (right == null) {
                    break;
                }

                emitBinaryOperation(opToken, right);

                // TODO: Avaliar se é realmente necessário utilizar este __acc__. Eu gostaria de evitar ele
                left = "__acc__";
            }
            else if (isRelationalOperator(peek().getId())) {

                Token op = advance();

                Object right = parseSimplePrimary();

                left = parseRelationalExpression(
                        left,
                        op,
                        right
                );
            }
            else {
                break;
            }
        }

        return "__acc__";
    }

    // TODO: Esta função deveria receber uma entrada de texto com o código a ser analisdo, e retornar o código BIP equivalente
    private boolean parseRelationalExpression(
            Object leftExpression,
            Token op,
            Object rightExpression)
    {
        if (!(leftExpression instanceof Integer)) throw new IllegalArgumentException();
        if (!(rightExpression instanceof Integer)) throw new IllegalArgumentException();

        int left = (Integer) leftExpression;
        int right = (Integer) rightExpression;

        // Consider left and right to be always integer for now
        // TODO: Implementar para quando left e/ou right forem váriaveis ao invés de inteiros
        emit("LDI " + left);
        emit("STO temp1");
        emit("LDI " + right);
        emit("STO temp2");
        emit("LD temp1");
        emit("SUB temp2");

        if (_conditionalStatement) {
            String branchType = switch (op.getId()) {
                case Constants.t_equality      -> "BNE";
                case Constants.t_inequality    -> "BEQ";
                case Constants.t_less_than     -> "BGT";
                case Constants.t_greater_than  -> "BLT";
                case Constants.t_less_equal    -> "BGE";
                case Constants.t_greater_equal -> "BLE";
                default ->
                        throw new IllegalArgumentException(
                                "Operador relacional inválido: " + op.getLexeme());
            };

            _conditionalFalseLabel = "__FALSE_IF_" + _ifsCount;

            emit(branchType + " " + _conditionalFalseLabel);
        }

        return switch (op.getId()) {
            case Constants.t_equality      -> left == right;
            case Constants.t_inequality    -> left != right;
            case Constants.t_less_than     -> left < right;
            case Constants.t_greater_than  -> left > right;
            case Constants.t_less_equal    -> left <= right;
            case Constants.t_greater_equal -> left >= right;
            default -> throw new IllegalArgumentException(
                    "Operador relacional inválido: " + op.getLexeme());
        };
    }

    // TODO: Esta função deveria receber uma entrada de texto com o código a ser analisdo, e retornar o código BIP equivalente
    private Object parseSimplePrimary() {
        Token t = peek();
        if (t == null) return null;
        int id = t.getId();

        if (id == Constants.t_variable) { advance(); return t.getLexeme();}
        if (id == Constants.t_number) { advance(); return Integer.parseInt(t.getLexeme()); }
        if (id == Constants.t_binary_number) {
            advance();
            return Long.parseLong(t.getLexeme().substring(2), 2);
        }
        if (id == Constants.t_hex_number) {
            advance();
            return Long.parseLong(t.getLexeme().substring(2), 16);
        }
        if (id == Constants.t_real_number) { advance(); return t.getLexeme(); }
        if (id == Constants.t_char_literal) { advance(); return (int) t.getLexeme().charAt(1); }
        if (id == Constants.t_true) { advance(); return true; }
        if (id == Constants.t_false) { advance(); return false; }

        advance();
        return null;
    }

    // TODO: O certo seria agrupar todas as constantes de type em um array/list e, parar verificar se um id é uma constante de type, usar list.contain(id)
    private boolean isTypeKeyword(int id) {
        return id == Constants.t_int || id == Constants.t_bool
            || id == Constants.t_short || id == Constants.t_long
            || id == Constants.t_float || id == Constants.t_double
            || id == Constants.t_decimal || id == Constants.t_char
            || id == Constants.t_string;
    }

    // TODO: O certo seria agrupar todas as constantes de operadores binário em um array/list e, parar verificar se um id é uma constante de type, usar list.contain(id)
    private boolean isBinaryOperator(int id) {
        return id == Constants.t_addition || id == Constants.t_subtraction
            || id == Constants.t_bit_and || id == Constants.t_bit_or
            || id == Constants.t_bit_xor || id == Constants.t_bit_shift_left
            || id == Constants.t_bit_shift_right;
    }

    // TODO: Retirar default. Não faz sentido ao meu ver
    private String bipBinaryOp(String lexeme) {
        return switch (lexeme) {
            case "+"  -> "ADD";
            case "-"  -> "SUB";
            case "&"  -> "AND";
            case "|"  -> "OR";
            case "^"  -> "XOR";
            case "<<" -> "SLL";
            case ">>" -> "SRL";
            default   -> "ADD";
        };
    }

    // TODO: Analisar se é realmente necessário
    private void skipDeclaration() {
        advance(); // type keyword or void
        if (peek() != null && peek().getId() == Constants.t_variable) {
            int lookahead = pos + 1;
            if (lookahead < tokens.size()
                    && tokens.get(lookahead).getId() == Constants.t_open_parentheses) {
                // function definition: skip param list and body
                skipBlock();
                return;
            }
        }
        skipUntilSemicolon();
    }

    // TODO: Analisar se é realmente necessário
    private void skipUntilSemicolon() {
        while (peek() != null && peek().getId() != Constants.t_semicolon) advance();
        if (peek() != null) advance();
    }

    // TODO: Analisar se é realmente necessário
    private void skipBlock() {
        while (peek() != null && peek().getId() != Constants.t_open_brace
                && peek().getId() != Constants.t_semicolon) {
            advance();
        }
        if (peek() == null) return;
        if (peek().getId() == Constants.t_semicolon) { advance(); return; }
        int depth = 0;
        while (peek() != null) {
            int id = peek().getId();
            advance();
            if (id == Constants.t_open_brace) depth++;
            else if (id == Constants.t_close_brace) { depth--; if (depth == 0) return; }
        }
    }

    // TODO: Analisar se é realmente necessário
    private String stripHash(String ref) {
        if (ref != null && ref.startsWith("#")) return ref.substring(1);
        return ref != null ? ref : "0";
    }

    // TODO: Analisar se é realmente necessário
    private void emit(String instruction) {
        codeLines.add("    " + instruction);
    }

    // TODO: Analisar se é realmente necessário
    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }

    // TODO: Analisar se é realmente necessário
    private Token advance() {
        return pos < tokens.size() ? tokens.get(pos++) : null;
    }

    // TODO: Deveria receber o .data e o .text como entrada e somente organizar em um arquivo .asm
    private String formatOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (String line : dataLines) sb.append("    ").append(line).append("\n");
        sb.append("\n.text\n");
        for (String line : codeLines) sb.append(line).append("\n");
        return sb.toString();
    }

    private boolean isRelationalOperator(int id) {
        return _relationalOPerationConstants.contains(id);
    }

    // TODO: Analisar se é realmente necessário
    private void replaceLastFalseJumpTarget(String target) {

        for (int i = codeLines.size() - 1; i >= 0; i--) {

            String line = codeLines.get(i);

            if (line.contains(_conditionalFalseLabel)) {
                codeLines.set(
                        i,
                        line.replace(_conditionalFalseLabel, target)
                );
                return;
            }
        }
    }

    // TODO: Analisar se é realmente necessário
    private void loadArrayIndex() {
        Token idx = peek();

        if (idx.getId() == Constants.t_number) {
            advance();
            emit("LDI " + idx.getLexeme());
        }
        else if (idx.getId() == Constants.t_variable) {
            advance();
            emit("LD " + idx.getLexeme());
        }
        else {
            throw new IllegalStateException(
                    "Índice inválido: " + idx.getLexeme());
        }

        emit("STO $indr");
    }

    // TODO: Analisar se é realmente necessário
    private boolean isAssignmentAhead() {
        int i = pos + 1;
        if (i < tokens.size() && tokens.get(i).getId() == Constants.t_open_bracket) {
            int depth = 1;
            i++;
            while (i < tokens.size() && depth > 0) {
                int tid = tokens.get(i).getId();
                if (tid == Constants.t_open_bracket) depth++;
                if (tid == Constants.t_close_bracket) depth--;
                i++;
            }
        }
        return i < tokens.size() && tokens.get(i).getId() == Constants.t_equals;
    }

    // TODO: Analisar se é realmente necessário
    private void loadOperand(Object operand) {

        if (operand instanceof Integer) {
            emit("LDI " + operand);
        }
        else if (operand instanceof String) {
            emit("LD " + operand);
        }
    }

    // TODO: Analisar se é realmente necessário
    private void emitBinaryOperation(
            Token operator,
            Object operand)
    {
        switch (operator.getLexeme()) {

            case "<<" -> {

                if (operand instanceof Integer value) {

                    emit("LDI " + value);
                    emit("STO __shift_tmp");
                    emit("SLL __shift_tmp");
                }
                else {
                    emit("SLL " + operand);
                }

                return;
            }

            case ">>" -> {

                if (operand instanceof Integer value) {

                    emit("LDI " + value);
                    emit("STO __shift_tmp");
                    emit("SRL __shift_tmp");
                }
                else {
                    emit("SRL " + operand);
                }

                return;
            }
        }

        String op = bipBinaryOp(operator.getLexeme());

        if (operand instanceof Integer) {
            emit(op + "I " + operand);
        }
        else {
            emit(op + " " + operand);
        }
    }
}

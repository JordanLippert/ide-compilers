package compiler.codegen;

import compiler.gals.Constants;
import compiler.gals.Symbol;
import compiler.gals.Token;

import java.util.*;

/**
 * Gerador de código Assembly para o processador BIP.
 *
 * @author Jordan Lippert
 * @author André Melo
 */
public class BipCodeGenerator implements ICodeGenerator {

    private static final Set<Integer> TYPE_KEYWORDS = Set.of(
        Constants.t_int,     Constants.t_bool,    Constants.t_short,
        Constants.t_long,    Constants.t_float,   Constants.t_double,
        Constants.t_decimal, Constants.t_char,    Constants.t_string,
        Constants.t_void
    );
    private static final int TEMP_BASE  = 1000;
    private static final int PARAM_BASE = 2000;
    private static final int RET_ADDR   = 2999;
    // -----------------------------------------------------------------------
    // Estado interno
    // -----------------------------------------------------------------------
    private final Map<String, String>       _variables     = new LinkedHashMap<>();
    private final List<String>              _textLines     = new ArrayList<>();
    private final Map<String, List<String>> _functionParams = new LinkedHashMap<>();
    private List<Token> _tokens;
    private int _pos;
    private int _nextTemp;
    private int _labelCounter;

    // -----------------------------------------------------------------------
    // ICodeGenerator — .data
    // -----------------------------------------------------------------------
    @Override
    public void generateVariable(Symbol symbol) {
        if (symbol == null) throw new IllegalArgumentException("symbol is null");
        if (Boolean.TRUE.equals(symbol.isFunction)) return;

        String entry;
        if (Boolean.TRUE.equals(symbol.isArray)) {
            int size = symbol.arraySize != null ? symbol.arraySize : 0;
            String zeros = "0" + ",0".repeat(Math.max(0, size - 1));
            entry = symbol.id + ": " + (size > 0 ? zeros : "0");
        } else {
            String val = symbol.value != null ? symbol.value.toString() : "0";
            entry = symbol.id + ": " + val;
        }
        _variables.put(symbol.id, entry);
    }

    // -----------------------------------------------------------------------
    // ICodeGenerator — .text
    // -----------------------------------------------------------------------
    @Override
    public void generateCode(List<Token> tokens) {
        _tokens       = tokens;
        _pos          = 0;
        _nextTemp     = TEMP_BASE;
        _labelCounter = 0;

        scanArrayInitializers();
        scanFunctionParameters();

        while (_pos < _tokens.size()) {
            parseStatement();
        }
        emit("HLT 0");
    }

    // -----------------------------------------------------------------------
    // ICodeGenerator — saída
    // -----------------------------------------------------------------------
    @Override
    public String getAssemblyCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (String entry : _variables.values()) {
            sb.append("    ").append(entry).append("\n");
        }
        sb.append("\n.text\n");
        // emit() already adds 4-space indent; emitLabel() adds none
        for (String line : _textLines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Pré-scans
    // -----------------------------------------------------------------------

    private void scanArrayInitializers() {
        int n = _tokens.size();
        for (int i = 0; i < n; i++) {
            if (!TYPE_KEYWORDS.contains(_tokens.get(i).getId())) continue;
            if (i+1 >= n || _tokens.get(i+1).getId() != Constants.t_variable) continue;
            String varName = _tokens.get(i+1).getLexeme();
            if (i+2 >= n || _tokens.get(i+2).getId() != Constants.t_open_bracket) continue;

            int j = i + 3;
            while (j < n && _tokens.get(j).getId() != Constants.t_close_bracket) j++;
            if (j >= n) continue; j++;
            if (j >= n || _tokens.get(j).getId() != Constants.t_equals) continue; j++;
            if (j >= n || _tokens.get(j).getId() != Constants.t_open_brace) continue; j++;

            List<String> values = new ArrayList<>();
            while (j < n && _tokens.get(j).getId() != Constants.t_close_brace) {
                if (isLiteralToken(_tokens.get(j).getId()))
                    values.add(_tokens.get(j).getLexeme());
                j++;
            }
            if (!values.isEmpty() && _variables.containsKey(varName))
                _variables.put(varName, varName + ": " + String.join(",", values));
        }
    }

    /** Coleta nomes e tipos de parâmetros de todas as funções definidas no código. */
    private void scanFunctionParameters() {
        int n = _tokens.size();
        for (int i = 0; i < n; i++) {
            int tid = _tokens.get(i).getId();
            if (!TYPE_KEYWORDS.contains(tid)) continue;
            if (i+1 >= n || _tokens.get(i+1).getId() != Constants.t_variable) continue;
            if (i+2 >= n || _tokens.get(i+2).getId() != Constants.t_open_parentheses) continue;

            String funcName = _tokens.get(i+1).getLexeme();
            if (_functionParams.containsKey(funcName)) continue;

            List<String> params = new ArrayList<>();
            int j = i + 3;
            while (j < n && _tokens.get(j).getId() != Constants.t_close_parentheses) {
                if (TYPE_KEYWORDS.contains(_tokens.get(j).getId())) {
                    j++; // skip type
                    if (j < n && _tokens.get(j).getId() == Constants.t_variable) {
                        String pName = _tokens.get(j++).getLexeme();
                        params.add(pName);
                        _variables.putIfAbsent(pName, pName + ": 0");
                        // skip [] for array params
                        if (j < n && _tokens.get(j).getId() == Constants.t_open_bracket) {
                            while (j < n && _tokens.get(j).getId() != Constants.t_close_bracket) j++;
                            j++;
                        }
                    }
                } else {
                    j++;
                }
            }
            _functionParams.put(funcName, params);
        }
    }

    // -----------------------------------------------------------------------
    // Dispatcher de statements
    // -----------------------------------------------------------------------
    private void parseStatement() {
        Token t = peek();
        if (t == null) return;
        int id = t.getId();

        if (id == Constants.t_open_brace || id == Constants.t_close_brace) { advance(); return; }
        if (TYPE_KEYWORDS.contains(id)) { skipDeclaration(); return; }

        if (id == Constants.t_if)     { parseIfStatement();      return; }
        if (id == Constants.t_while)  { parseWhileStatement();   return; }
        if (id == Constants.t_do)     { parseDoWhileStatement(); return; }
        if (id == Constants.t_for)    { parseForStatement();     return; }
        if (id == Constants.t_return) { parseReturnStatement();  return; }
        if (id == Constants.t_else)   { advance(); return; }
        if (id == Constants.t_read)   { parseReadStatement();    return; }
        if (id == Constants.t_write)  { parseWriteStatement();   return; }

        // Chamada de função como statement: funcname ( args ) ;
        if (id == Constants.t_variable && isFunctionCallAhead()) {
            String funcName = advance().getLexeme();
            advance(); // (
            _nextTemp = TEMP_BASE;
            emitFunctionCallArgs(funcName);
            match(Constants.t_semicolon);
            return;
        }
        if (id == Constants.t_variable && isAssignmentAhead()) {
            parseAssignmentStatement(); return;
        }
        advance();
    }

    // -----------------------------------------------------------------------
    // Desvio condicional
    // -----------------------------------------------------------------------

    private void parseIfStatement() {
        advance(); // 'if'
        String branchFalse = parseCondition();
        String labelFalse  = newLabel("FIMSE");
        emit(branchFalse + " " + labelFalse);

        parseBlock(); // then

        if (check(Constants.t_else)) {
            advance(); // 'else'
            String labelEnd = newLabel("ELSE");
            emit("JMP " + labelEnd);
            emitLabel(labelFalse);
            parseBlock(); // else
            emitLabel(labelEnd);
        } else {
            emitLabel(labelFalse);
        }
    }

    // -----------------------------------------------------------------------
    // Laços
    // -----------------------------------------------------------------------

    private void parseWhileStatement() {
        advance(); // 'while'
        String labelStart = newLabel("BEGIN_WHILE");
        emitLabel(labelStart);

        String branchFalse = parseCondition();
        String labelEnd    = newLabel("END_WHILE");
        emit(branchFalse + " " + labelEnd);

        parseBlock();

        emit("JMP " + labelStart);
        emitLabel(labelEnd);
    }

    private void parseDoWhileStatement() {
        advance(); // 'do'
        String labelStart = newLabel("DO_WHILE");
        emitLabel(labelStart);
        parseBlock();

        match(Constants.t_while);
        match(Constants.t_open_parentheses);
        _nextTemp = TEMP_BASE;
        String branchTrue = parseInlineConditionPositive();
        match(Constants.t_close_parentheses);
        match(Constants.t_semicolon);

        emit(branchTrue + " " + labelStart);
    }

    private void parseForStatement() {
        advance(); // 'for'
        match(Constants.t_open_parentheses);

        // Inicialização
        _nextTemp = TEMP_BASE;
        parseForInit();
        match(Constants.t_semicolon);

        // Condição — emite rótulo de início do laço
        String labelStart = newLabel("BEGIN_FOR");
        String labelEnd   = newLabel("END_FOR");
        emitLabel(labelStart);

        if (!check(Constants.t_semicolon)) {
            _nextTemp = TEMP_BASE;
            String branchFalse = parseInlineCondition();
            emit(branchFalse + " " + labelEnd);
        }
        match(Constants.t_semicolon);

        // Salva a seção de atualização e a pula por enquanto
        int updateStart = _pos;
        int depth = 0;
        while (_pos < _tokens.size()) {
            int id = _tokens.get(_pos).getId();
            if (id == Constants.t_open_parentheses) depth++;
            if (id == Constants.t_close_parentheses) {
                if (depth == 0) break;
                depth--;
            }
            _pos++;
        }
        int updateEnd = _pos;
        match(Constants.t_close_parentheses);

        // Corpo do laço
        parseBlock();

        // Emite a atualização (i++, i = i+1, etc.)
        int savedPos = _pos;
        _pos = updateStart;
        _nextTemp = TEMP_BASE;
        parseForUpdate(updateEnd);
        _pos = savedPos;

        emit("JMP " + labelStart);
        emitLabel(labelEnd);
    }

    /** Inicialização do for: 'int i = 0' ou 'i = 0'. Para antes do ';'. */
    private void parseForInit() {
        if (check(Constants.t_semicolon)) return;

        if (TYPE_KEYWORDS.contains(peek().getId())) {
            advance(); // tipo
            if (!check(Constants.t_variable)) return;
            String varName = advance().getLexeme();
            if (check(Constants.t_equals)) {
                advance(); // =
                parseExpression();
                emit("STO " + varName);
            }
        } else if (check(Constants.t_variable) && isAssignmentAhead()) {
            parseAssignmentNoSemi();
        }
    }

    /** Atualização do for: i++, i--, i = i+1. Para ao atingir endPos. */
    private void parseForUpdate(int endPos) {
        while (_pos < endPos) {
            if (check(Constants.t_comma)) { advance(); continue; }
            if (!check(Constants.t_variable)) { advance(); continue; }

            Token after = peek(1);
            if (after == null) { advance(); break; }

            if (after.getId() == Constants.t_increment) {
                String n = advance().getLexeme(); advance();
                emit("LD " + n); emit("ADDI 1"); emit("STO " + n);
            } else if (after.getId() == Constants.t_decrement) {
                String n = advance().getLexeme(); advance();
                emit("LD " + n); emit("SUBI 1"); emit("STO " + n);
            } else if (isAssignmentAhead()) {
                parseAssignmentNoSemi();
            } else {
                advance();
            }
        }
    }

    /** Analisa { lista de statements }. */
    private void parseBlock() {
        match(Constants.t_open_brace);
        while (!check(Constants.t_close_brace) && _pos < _tokens.size()) {
            parseStatement();
        }
        match(Constants.t_close_brace);
    }

    // -----------------------------------------------------------------------
    // Condições relacionais
    // -----------------------------------------------------------------------

    /** Analisa (lhs relop rhs) e retorna instrução de desvio quando condição é FALSA. */
    private String parseCondition() {
        match(Constants.t_open_parentheses);
        _nextTemp = TEMP_BASE;
        String result = parseInlineCondition();
        match(Constants.t_close_parentheses);
        return result;
    }

    /** Analisa lhs relop rhs sem parênteses; retorna branch negado (pula bloco se falso). */
    private String parseInlineCondition() {
        parseExpression();
        int lhsTemp = allocTemp();
        emit("STO " + lhsTemp);

        Token op = peek();
        if (op != null && isRelationalOp(op.getId())) {
            String opLex = op.getLexeme();
            advance();
            parseExpression();
            int rhsTemp = allocTemp();
            emit("STO " + rhsTemp);
            emit("LD " + lhsTemp);
            emit("SUB " + rhsTemp);
            return negatedBranch(opLex);
        }
        // Sem operador relacional: considera não-zero como verdadeiro
        emit("LD " + lhsTemp);
        return "BEQ";
    }

    /** Analisa lhs relop rhs; retorna branch positivo (repete laço se verdadeiro). */
    private String parseInlineConditionPositive() {
        parseExpression();
        int lhsTemp = allocTemp();
        emit("STO " + lhsTemp);

        Token op = peek();
        if (op != null && isRelationalOp(op.getId())) {
            String opLex = op.getLexeme();
            advance();
            parseExpression();
            int rhsTemp = allocTemp();
            emit("STO " + rhsTemp);
            emit("LD " + lhsTemp);
            emit("SUB " + rhsTemp);
            return positiveBranch(opLex);
        }
        emit("LD " + lhsTemp);
        return "BNE";
    }

    // -----------------------------------------------------------------------
    // Return
    // -----------------------------------------------------------------------

    private void parseReturnStatement() {
        advance(); // 'return'
        if (!check(Constants.t_semicolon)) {
            _nextTemp = TEMP_BASE;
            parseExpression();
            emit("STO " + RET_ADDR);
        }
        match(Constants.t_semicolon);
        emit("RETURN");
    }

    // -----------------------------------------------------------------------
    // Funções
    // -----------------------------------------------------------------------

    private void parseFunctionDefinition(String funcName) {
        advance(); // nome da função
        advance(); // (

        List<String> params = _functionParams.getOrDefault(funcName, Collections.emptyList());

        // Pula a lista de parâmetros (já analisada no pré-scan)
        int depth = 0;
        while (_pos < _tokens.size()) {
            int id = _tokens.get(_pos).getId();
            if (id == Constants.t_open_parentheses) depth++;
            if (id == Constants.t_close_parentheses) { if (depth == 0) break; depth--; }
            _pos++;
        }
        match(Constants.t_close_parentheses);

        // Pula o corpo da função na execução principal
        String skipLabel = newLabel("FUNCTION");
        emit("JMP " + skipLabel);

        // Rótulo de entrada da função
        emitLabel(funcName);

        // Copia parâmetros dos slots de chamada para variáveis locais
        for (int i = 0; i < params.size(); i++) {
            emit("LD " + (PARAM_BASE + i));
            emit("STO " + params.get(i));
        }

        // Corpo
        parseBlock();

        // RETURN padrão (caso não haja return explícito)
        emit("RETURN");

        emitLabel(skipLabel);
    }

    /**
     * Emite avaliação dos argumentos e instrução CALL.
     * Valida quantidade de parâmetros e emite erros/avisos se necessário.
     */
    private void emitFunctionCallArgs(String funcName) {
        List<String> expectedParams = _functionParams.get(funcName);
        int paramIdx = 0;

        while (!check(Constants.t_close_parentheses) && _pos < _tokens.size()) {
            if (check(Constants.t_comma)) { advance(); continue; }
            _nextTemp = TEMP_BASE;
            parseExpression();
            emit("STO " + (PARAM_BASE + paramIdx));
            paramIdx++;
        }
        match(Constants.t_close_parentheses);

        // Validação de compatibilidade de parâmetros
        if (expectedParams == null) {
            emit("; AVISO: funcao '" + funcName + "' nao foi declarada");
        } else if (paramIdx != expectedParams.size()) {
            emit("; ERRO: funcao '" + funcName + "' espera " + expectedParams.size()
                 + " parametro(s) mas recebeu " + paramIdx);
        }

        emit("CALL " + funcName);
    }

    // -----------------------------------------------------------------------
    // read / write
    // -----------------------------------------------------------------------

    private void parseReadStatement() {
        advance();
        match(Constants.t_open_parentheses);

        while (!check(Constants.t_close_parentheses) && _pos < _tokens.size()) {
            if (check(Constants.t_comma)) { advance(); continue; }
            if (check(Constants.t_variable)) {
                Token varTok = advance();
                String varName = varTok.getLexeme();
                if (check(Constants.t_open_bracket)) {
                    advance();
                    emitSetIndr();
                    match(Constants.t_close_bracket);
                    emit("LD $in_port");
                    emit("STOV " + varName);
                } else {
                    emit("LD $in_port");
                    emit("STO " + varName);
                }
            } else { advance(); }
        }
        match(Constants.t_close_parentheses);
        match(Constants.t_semicolon);
    }

    private void parseWriteStatement() {
        advance();
        match(Constants.t_open_parentheses);

        while (!check(Constants.t_close_parentheses) && _pos < _tokens.size()) {
            if (check(Constants.t_comma)) { advance(); continue; }
            _nextTemp = TEMP_BASE;
            parseExpression();
            emit("STO $out_port");
        }
        match(Constants.t_close_parentheses);
        match(Constants.t_semicolon);
    }

    // -----------------------------------------------------------------------
    // Atribuição
    // -----------------------------------------------------------------------

    private void parseAssignmentStatement() {
        _nextTemp = TEMP_BASE;
        Token varTok = advance();
        String varName = varTok.getLexeme();

        if (check(Constants.t_open_bracket)) {
            advance(); // [
            emitLoadIndex();
            match(Constants.t_close_bracket);
            int idxTemp = allocTemp();
            emit("STO " + idxTemp);
            match(Constants.t_equals);
            parseExpression();
            int resTemp = allocTemp();
            emit("STO " + resTemp);
            emit("LD " + idxTemp);
            emit("STO $indr");
            emit("LD " + resTemp);
            emit("STOV " + varName);
        } else {
            advance(); // '='
            parseExpression();
            emit("STO " + varName);
        }
        match(Constants.t_semicolon);
    }

    /** Atribuição sem consumir ';' — usada em for init/update. */
    private void parseAssignmentNoSemi() {
        _nextTemp = TEMP_BASE;
        Token varTok = advance();
        String varName = varTok.getLexeme();

        if (check(Constants.t_open_bracket)) {
            advance(); // [
            emitLoadIndex();
            int idxTemp = allocTemp();
            emit("STO " + idxTemp);
            match(Constants.t_close_bracket);
            match(Constants.t_equals);
            parseExpression();
            int resTemp = allocTemp();
            emit("STO " + resTemp);
            emit("LD " + idxTemp);
            emit("STO $indr");
            emit("LD " + resTemp);
            emit("STOV " + varName);
        } else {
            advance(); // '='
            parseExpression();
            emit("STO " + varName);
        }
    }

    // -----------------------------------------------------------------------
    // Expressões
    // -----------------------------------------------------------------------

    private void parseExpression() {
        parsePrimary();

        while (peek() != null && isBinaryOp(peek().getId())) {
            String opLex = advance().getLexeme();
            Token rhs = peek();
            if (rhs == null) break;

            if (isLiteralToken(rhs.getId())) {
                advance();
                emit(bipOpImm(opLex) + " " + rhs.getLexeme());

            } else if (rhs.getId() == Constants.t_variable) {
                advance();
                String rhsName = rhs.getLexeme();

                if (check(Constants.t_open_parentheses)) {
                    // Chamada de função no lado direito do operador
                    advance(); // (
                    int tempAcc = allocTemp();
                    emit("STO " + tempAcc);
                    emitFunctionCallArgs(rhsName);
                    emit("LD " + RET_ADDR);
                    int tempRet = allocTemp();
                    emit("STO " + tempRet);
                    emit("LD " + tempAcc);
                    emit(bipOpMem(opLex) + " " + tempRet);

                } else if (check(Constants.t_open_bracket)) {
                    // Acesso a vetor no lado direito
                    advance(); // [
                    int leftTemp = allocTemp();
                    emit("STO " + leftTemp);
                    emitSetIndr();
                    match(Constants.t_close_bracket);
                    emit("LDV " + rhsName);
                    int vecTemp = allocTemp();
                    emit("STO " + vecTemp);
                    emit("LD " + leftTemp);
                    emit(bipOpMem(opLex) + " " + vecTemp);
                } else {
                    emit(bipOpMem(opLex) + " " + rhsName);
                }
            }
        }
    }

    private void parsePrimary() {
        Token t = peek();
        if (t == null) return;

        if (isLiteralToken(t.getId())) {
            advance();
            emit("LDI " + t.getLexeme());

        } else if (t.getId() == Constants.t_variable) {
            advance();
            String name = t.getLexeme();

            if (check(Constants.t_open_parentheses)) {
                // Chamada de função
                advance(); // (
                emitFunctionCallArgs(name);
                emit("LD " + RET_ADDR);

            } else if (check(Constants.t_open_bracket)) {
                advance(); // [
                emitSetIndr();
                match(Constants.t_close_bracket);
                emit("LDV " + name);
            } else {
                emit("LD " + name);
            }

        } else if (t.getId() == Constants.t_open_parentheses) {
            advance();
            parseExpression();
            match(Constants.t_close_parentheses);
        }
    }

    // -----------------------------------------------------------------------
    // Declarações
    // -----------------------------------------------------------------------

    private void skipDeclaration() {
        advance(); // consome keyword de tipo

        Token name  = peek();
        Token after = peek(1);

        // Definição de função
        if (name  != null && name.getId()  == Constants.t_variable &&
            after != null && after.getId() == Constants.t_open_parentheses) {
            parseFunctionDefinition(name.getLexeme());
            return;
        }
        // Declaração de vetor — pula até ';'
        if (name  != null && name.getId()  == Constants.t_variable &&
            after != null && after.getId() == Constants.t_open_bracket) {
            skipToSemicolon();
            return;
        }
        // Variável com inicialização — gera código de atribuição
        if (name  != null && name.getId()  == Constants.t_variable &&
            after != null && after.getId() == Constants.t_equals) {
            _nextTemp = TEMP_BASE;
            String varName = name.getLexeme();
            advance(); advance(); // var, =
            parseExpression();
            emit("STO " + varName);
            match(Constants.t_semicolon);
            return;
        }
        // Variável sem inicialização — pula
        skipToSemicolon();
    }

    private void skipToSemicolon() {
        while (_pos < _tokens.size()) {
            if (_tokens.get(_pos++).getId() == Constants.t_semicolon) return;
        }
    }

    // -----------------------------------------------------------------------
    // Helpers de índice de vetor
    // -----------------------------------------------------------------------

    private void emitLoadIndex() {
        Token t = peek();
        if (t == null) return;
        if (isLiteralToken(t.getId())) { advance(); emit("LDI " + t.getLexeme()); }
        else if (t.getId() == Constants.t_variable) { advance(); emit("LD " + t.getLexeme()); }
    }

    private void emitSetIndr() {
        emitLoadIndex();
        emit("STO $indr");
    }

    // -----------------------------------------------------------------------
    // Alocador de temporários e rótulos
    // -----------------------------------------------------------------------

    private int    allocTemp() { return _nextTemp++; }
    private String newLabel(String label)  { return label + (++_labelCounter); }

    private void emitLabel(String label) {
        _textLines.add(label + ":"); // sem indentação
    }

    // -----------------------------------------------------------------------
    // Lookahead helpers
    // -----------------------------------------------------------------------

    private boolean isAssignmentAhead() {
        Token next = peek(1);
        if (next == null) return false;
        int id = next.getId();
        return id == Constants.t_equals || id == Constants.t_open_bracket;
    }

    private boolean isFunctionCallAhead() {
        Token next = peek(1);
        return next != null && next.getId() == Constants.t_open_parentheses;
    }

    private boolean isBinaryOp(int id) {
        return id == Constants.t_addition       || id == Constants.t_subtraction
            || id == Constants.t_bit_and        || id == Constants.t_bit_or
            || id == Constants.t_bit_xor
            || id == Constants.t_bit_shift_left || id == Constants.t_bit_shift_right;
    }

    private boolean isRelationalOp(int id) {
        return id == Constants.t_greater_than  || id == Constants.t_less_than
            || id == Constants.t_greater_equal || id == Constants.t_less_equal
            || id == Constants.t_equality      || id == Constants.t_inequality;
    }

    private boolean isLiteralToken(int id) {
        return id == Constants.t_number        || id == Constants.t_hex_number
            || id == Constants.t_binary_number || id == Constants.t_real_number;
    }

    // -----------------------------------------------------------------------
    // Mapeamento operador → instrução BIP
    // -----------------------------------------------------------------------

    private String bipOpMem(String op) {
        return switch (op) {
            case "+"  -> "ADD";  case "-"  -> "SUB";
            case "&"  -> "AND";  case "|"  -> "OR";
            case "^"  -> "XOR";
            case "<<" -> "SLL";  case ">>" -> "SRL";
            default -> throw new IllegalArgumentException("Operador desconhecido: " + op);
        };
    }

    private String bipOpImm(String op) {
        return switch (op) {
            case "+"  -> "ADDI"; case "-"  -> "SUBI";
            case "&"  -> "ANDI"; case "|"  -> "ORI";
            case "^"  -> "XORI";
            case "<<" -> "SLL";  case ">>" -> "SRL";
            default -> throw new IllegalArgumentException("Operador desconhecido: " + op);
        };
    }

    /** Instrução de desvio quando a condição é FALSA (para pular bloco). */
    private String negatedBranch(String op) {
        return switch (op) {
            case ">"  -> "BLE"; case "<"  -> "BGE";
            case ">=" -> "BLT"; case "<=" -> "BGT";
            case "==" -> "BNE"; case "!=" -> "BEQ";
            default -> "BEQ";
        };
    }

    /** Instrução de desvio quando a condição é VERDADEIRA (para repetir laço). */
    private String positiveBranch(String op) {
        return switch (op) {
            case ">"  -> "BGT"; case "<"  -> "BLT";
            case ">=" -> "BGE"; case "<=" -> "BLE";
            case "==" -> "BEQ"; case "!=" -> "BNE";
            default -> "BNE";
        };
    }

    // -----------------------------------------------------------------------
    // Utilitários de token
    // -----------------------------------------------------------------------

    private Token peek()         { return _pos < _tokens.size() ? _tokens.get(_pos) : null; }
    private Token peek(int off)  { int i = _pos+off; return (i>=0 && i<_tokens.size()) ? _tokens.get(i) : null; }
    private Token advance()      { return _pos < _tokens.size() ? _tokens.get(_pos++) : null; }
    private boolean check(int id){ Token t = peek(); return t != null && t.getId() == id; }
    private boolean match(int id){ if (check(id)) { advance(); return true; } return false; }
    private void emit(String s)  { _textLines.add("    " + s); }
}

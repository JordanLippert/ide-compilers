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
        buildDataSection();
        buildCodeSection();
        return formatOutput();
    }

    private void buildDataSection() {
        for (Symbol s : symbolTable) {
            if (Boolean.TRUE.equals(s.isFunction)) continue;
            if (Boolean.TRUE.equals(s.isParameter)) continue;

            if (Boolean.TRUE.equals(s.isArray)) {
                int size = s.arraySize != null ? s.arraySize : 10;
                dataLines.add(s.id + ": [" + size + "]");
            } else {
                Object initVal = s.value != null ? s.value : 0;
                dataLines.add(s.id + ": " + initVal);
            }
        }
    }

    private void buildCodeSection() {
        pos = 0;
        while (pos < tokens.size()) {
            parseStatement();
        }
        emit("HLT 0");
    }

    private void parseStatement() {
        Token t = peek();
        if (t == null) return;
        int id = t.getId();

        if (isTypeKeyword(id) || id == Constants.t_void) {
            skipDeclaration();
            return;
        }
        if (id == Constants.t_read) { parseReadStatement(); return; }
        if (id == Constants.t_write) { parseWriteStatement(); return; }
        if (id == Constants.t_return) { skipUntilSemicolon(); return; }
        if (id == Constants.t_if) { parseConditionalStatement(); return; }
        advance();
    }

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

    // Tem que fazer mostrar no console to JPANEL também
    private void parseWriteStatement() {
        advance(); // 'write'
        advance(); // '('
        while (peek() != null && peek().getId() != Constants.t_close_parentheses) {
            Object result = parseExpression();
            emit("STO $out_port");
        }
        if (peek() != null) advance(); // ')'
        if (peek() != null && peek().getId() == Constants.t_semicolon) advance();
    }

    private Object parseExpression() {
        Object left = parseSimplePrimary();
        if (left == null) return null;

        while (peek() != null) {
            if (isBinaryOperator(peek().getId())) {
                if (left instanceof Integer) {
                    emit("LDI " + left);
                }
                Token opToken = peek();
                advance();
                Object right = parseSimplePrimary();
                if (right == null) break;
                emit(bipBinaryOp(opToken.getLexeme()) + (right instanceof Integer ? "I" : "") + " " + right);
            }
            else if (isRelationalOperator(peek().getId())) {
                Token op = advance();
                Object right = parseSimplePrimary();
                left = parseRelationalExpression(left, op, right);
            }
            else {
                break;
            }
        }

        return left;
    }

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

    private Object parseSimplePrimary() {
        Token t = peek();
        if (t == null) return null;
        int id = t.getId();

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

    private boolean isTypeKeyword(int id) {
        return id == Constants.t_int || id == Constants.t_bool
            || id == Constants.t_short || id == Constants.t_long
            || id == Constants.t_float || id == Constants.t_double
            || id == Constants.t_decimal || id == Constants.t_char
            || id == Constants.t_string;
    }

    private boolean isBinaryOperator(int id) {
        return id == Constants.t_addition || id == Constants.t_subtraction
            || id == Constants.t_bit_and || id == Constants.t_bit_or
            || id == Constants.t_bit_xor || id == Constants.t_bit_shift_left
            || id == Constants.t_bit_shift_right;
    }

    private String bipBinaryOp(String lexeme) {
        return switch (lexeme) {
            case "+"  -> "ADD";
            case "-"  -> "SUB";
            case "&"  -> "AND";
            case "|"  -> "OR";
            case "^"  -> "XOR";
            case "<<" -> "SHL";
            case ">>" -> "SHR";
            default   -> "ADD";
        };
    }

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

    private void skipUntilSemicolon() {
        while (peek() != null && peek().getId() != Constants.t_semicolon) advance();
        if (peek() != null) advance();
    }

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

    private String stripHash(String ref) {
        if (ref != null && ref.startsWith("#")) return ref.substring(1);
        return ref != null ? ref : "0";
    }

    private void emit(String instruction) {
        codeLines.add("    " + instruction);
    }

    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }

    private Token advance() {
        return pos < tokens.size() ? tokens.get(pos++) : null;
    }

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
}

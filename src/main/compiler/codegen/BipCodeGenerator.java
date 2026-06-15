package compiler.codegen;

import compiler.gals.Constants;
import compiler.gals.Symbol;
import compiler.gals.Token;

import java.util.ArrayList;
import java.util.List;

public class BipCodeGenerator {

    private final List<Token> tokens;
    private final List<Symbol> symbolTable;
    private final List<String> dataLines = new ArrayList<>();
    private final List<String> codeLines = new ArrayList<>();
    private int pos = 0;

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
        if (id == Constants.t_write) { parseWriteStatement(); return; }
        if (id == Constants.t_return) { skipUntilSemicolon(); return; }
        if (id == Constants.t_if || id == Constants.t_while
                || id == Constants.t_do || id == Constants.t_for) {
            skipBlock(); return;
        }
        advance();
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
        if (left instanceof Integer) {
            emit("LDI " + left);
        }

        while (peek() != null && isBinaryOperator(peek().getId())) {
            Token opToken = peek();
            advance();
            Object right = parseSimplePrimary();
            if (right == null) break;

                emit(bipBinaryOp(opToken.getLexeme()) + (right instanceof Integer ? "I" : "") + " " + right);
        }

        return left;
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
        sb.append("\n.code\n");
        for (String line : codeLines) sb.append(line).append("\n");
        return sb.toString();
    }
}

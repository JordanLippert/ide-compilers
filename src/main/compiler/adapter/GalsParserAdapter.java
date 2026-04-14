package compiler.adapter;

import compiler.gals.*;
import compiler.model.CompilationResult;
import compiler.model.ErrorMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Implementação temporária do GalsAdapter
 * Será substituída quando as classes GALS forem geradas
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class GalsParserAdapter implements IGalsAdapter {
    private String sourceCode;
    private List<Token> tokens = new ArrayList<>();
    private static final int MAX_EXPECTED_TOKENS = 12;
    private static final int MAX_TERMINAL_TOKEN_ID = resolveMaxTerminalTokenId();
    private static final Map<Integer, String> TOKEN_NAMES = buildTokenNames();

    private final Semantico _semantico;
    private final Sintatico _sintatico;

    public GalsParserAdapter(Sintatico sintatico, Semantico semantico) {
        this._sintatico = sintatico;
        this._semantico = semantico;
    }

    @Override
    public CompilationResult performLexicalAnalysis(String sourceCode) {
        this.sourceCode = sourceCode;
        tokens.clear();

        try {
            Lexico lexico = new Lexico(sourceCode);
            Token token;

            while ((token = lexico.nextToken()) != null) {
                tokens.add(token);
            }

            return CompilationResult.success(new ArrayList<>(tokens));
        } catch (Exception e) {
            return CompilationResult.error(mapError(e));
        }
    }

    @Override
    public CompilationResult performSyntacticAnalysis(String sourceCode) {
        this.sourceCode = sourceCode;

        try {
            Lexico lexico = new Lexico(sourceCode);
            Semantico dummy = new Semantico();

            // TODO: Syntactic analysis may also execute semantic actions. But there may be a way to separate it
            _sintatico.parse(lexico, dummy);

            return CompilationResult.success();
        } catch (Exception e) {
            return CompilationResult.error(mapError(e));
        }
    }

    @Override
    public CompilationResult performSemanticAnalysis(String sourceCode) {
        this.sourceCode = sourceCode;

        try {
            Lexico lexico = new Lexico(sourceCode);
            _sintatico.parse(lexico, _semantico);

            return CompilationResult.success();
        } catch (Exception e) {
            return CompilationResult.error(mapError(e));
        }
    }

    @Override
    public List<Object> getTokens() {
        return new ArrayList<>(tokens);
    }

    private ErrorMessage mapError(Exception e) {

        ErrorMessage.Builder builder = ErrorMessage.builder();

        if (e instanceof AnalysisError ae) {
            builder
                .position(ae.getPosition())
                .line(calculateLine(ae.getPosition()));

            if (e instanceof LexicalError) {
                builder.message(ae.getMessage());
                builder.type("LEXICAL");
            } else if (e instanceof SyntacticError) {
                builder.message(buildDetailedSyntacticMessage((SyntacticError) e));
                builder.type("SYNTACTIC");
            } else if (e instanceof SemanticError) {
                builder.message(ae.getMessage());
                builder.type("SEMANTIC");
            } else {
                builder.message(ae.getMessage());
                builder.type("ANALYSIS");
            }

            return builder.build();
        }

        // fallback for non-GALS exceptions
        return ErrorMessage.builder()
                .type("UNKNOWN")
                .message(e.getMessage())
                .build();
    }

    private int calculateLine(int position) {
        if (position < 0) return 1;

        int line = 1;
        for (int i = 0; i < position && i < sourceCode.length(); i++) {
            if (sourceCode.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private int calculateColumn(int position) {
        if (position < 0) {
            return 1;
        }

        int column = 1;
        for (int i = 0; i < position && i < sourceCode.length(); i++) {
            if (sourceCode.charAt(i) == '\n') {
                column = 1;
            } else {
                column++;
            }
        }
        return column;
    }

    private String buildDetailedSyntacticMessage(SyntacticError error) {
        int errorPosition = error.getPosition();
        int line = calculateLine(errorPosition);
        int column = calculateColumn(errorPosition);

        SyntaxContext context = traceSyntacticContext(errorPosition);
        Token foundToken = context != null ? context.foundToken : null;
        int parserState = context != null ? context.state : -1;
        List<String> expectedTokens = context != null ? context.expectedTokens : new ArrayList<>();

        String found = formatFoundToken(foundToken);
        String expected = formatExpectedTokens(expectedTokens);

        if (parserState >= 0) {
            return String.format(
                "Erro sintático na linha %d, coluna %d: encontrado %s; esperado: %s. [estado %d]",
                line, column, found, expected, parserState
            );
        }

        return String.format(
            "Erro sintático na linha %d, coluna %d: encontrado %s; esperado: %s.",
            line, column, found, expected
        );
    }

    private SyntaxContext traceSyntacticContext(int expectedErrorPosition) {
        try {
            Lexico scanner = new Lexico(sourceCode);
            Stack<Integer> stack = new Stack<>();
            stack.push(0);

            Token currentToken = scanner.nextToken();
            Token previousToken = null;

            while (true) {
                if (currentToken == null) {
                    int eofPosition = previousToken != null
                        ? previousToken.getPosition() + previousToken.getLexeme().length()
                        : 0;
                    currentToken = new Token(Constants.DOLLAR, "$", eofPosition);
                }

                int tokenId = currentToken.getId();
                int state = stack.peek();

                if (tokenId <= 0 || tokenId - 1 >= ParserConstants.PARSER_TABLE[state].length) {
                    return new SyntaxContext(state, currentToken, new ArrayList<>());
                }

                int[] cmd = ParserConstants.PARSER_TABLE[state][tokenId - 1];

                switch (cmd[0]) {
                    case ParserConstants.SHIFT:
                        stack.push(cmd[1]);
                        previousToken = currentToken;
                        currentToken = scanner.nextToken();
                        break;
                    case ParserConstants.REDUCE:
                        int[] production = ParserConstants.PRODUCTIONS[cmd[1]];
                        for (int i = 0; i < production[1]; i++) {
                            stack.pop();
                        }
                        int oldState = stack.peek();
                        stack.push(ParserConstants.PARSER_TABLE[oldState][production[0] - 1][1]);
                        break;
                    case ParserConstants.ACTION:
                        int action = ParserConstants.FIRST_SEMANTIC_ACTION + cmd[1] - 1;
                        stack.push(ParserConstants.PARSER_TABLE[state][action][1]);
                        break;
                    case ParserConstants.ACCEPT:
                        return new SyntaxContext(state, currentToken, new ArrayList<>());
                    case ParserConstants.ERROR:
                        if (expectedErrorPosition < 0 || currentToken.getPosition() == expectedErrorPosition) {
                            return new SyntaxContext(state, currentToken, collectExpectedTokens(state));
                        }
                        return new SyntaxContext(state, currentToken, collectExpectedTokens(state));
                    default:
                        return new SyntaxContext(state, currentToken, new ArrayList<>());
                }
            }
        } catch (LexicalError lexicalError) {
            return null;
        }
    }

    private List<String> collectExpectedTokens(int state) {
        List<String> expected = new ArrayList<>();

        for (int tokenId = 1; tokenId <= MAX_TERMINAL_TOKEN_ID; tokenId++) {
            int[] cmd = ParserConstants.PARSER_TABLE[state][tokenId - 1];
            if (cmd[0] != ParserConstants.ERROR) {
                expected.add(formatTokenName(tokenId));
            }
        }

        return expected;
    }

    private String formatFoundToken(Token token) {
        if (token == null) {
            return "fim de arquivo";
        }

        if (token.getId() == Constants.DOLLAR || "$".equals(token.getLexeme())) {
            return "fim de arquivo";
        }

        String lexeme = token.getLexeme();
        if (lexeme == null || lexeme.isBlank()) {
            return formatTokenName(token.getId());
        }

        return "'" + lexeme + "'";
    }

    private String formatExpectedTokens(List<String> expectedTokens) {
        if (expectedTokens == null || expectedTokens.isEmpty()) {
            return "token válido";
        }

        int limit = Math.min(MAX_EXPECTED_TOKENS, expectedTokens.size());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(expectedTokens.get(i));
        }

        if (expectedTokens.size() > limit) {
            builder.append(", ...");
        }

        return builder.toString();
    }

    private static int resolveMaxTerminalTokenId() {
        int max = Constants.DOLLAR;

        for (java.lang.reflect.Field field : Constants.class.getFields()) {
            if (field.getType() == int.class && field.getName().startsWith("t_")) {
                try {
                    int value = field.getInt(null);
                    if (value > max) {
                        max = value;
                    }
                } catch (IllegalAccessException ignored) {
                    // Interface constants are public, static and final.
                }
            }
        }

        return max;
    }

    private static Map<Integer, String> buildTokenNames() {
        Map<Integer, String> names = new HashMap<>();
        names.put(Constants.DOLLAR, "fim de arquivo");

        for (java.lang.reflect.Field field : Constants.class.getFields()) {
            if (field.getType() != int.class || !field.getName().startsWith("t_")) {
                continue;
            }

            try {
                int tokenId = field.getInt(null);
                names.put(tokenId, "'" + field.getName().substring(2) + "'");
            } catch (IllegalAccessException ignored) {
                // Interface constants are public, static and final.
            }
        }

        return names;
    }

    private String formatTokenName(int tokenId) {
        return TOKEN_NAMES.getOrDefault(tokenId, "'token_" + tokenId + "'");
    }

    private static class SyntaxContext {
        private final int state;
        private final Token foundToken;
        private final List<String> expectedTokens;

        private SyntaxContext(int state, Token foundToken, List<String> expectedTokens) {
            this.state = state;
            this.foundToken = foundToken;
            this.expectedTokens = expectedTokens;
        }
    }
}

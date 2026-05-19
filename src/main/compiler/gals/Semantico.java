package compiler.gals;

import java.util.*;

public class Semantico implements Constants
{
    private final Stack<Literal> literalStack = new Stack<>();
    private final Stack<Scope> scopeStack = new Stack<>();
    private final Stack<OperationType> operatorStack = new Stack<>();

    private final List<Symbol> symbolsTable = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    private SymbolType currentSymbolType = null;
    private Scope currentScope = new Scope("global", null, true);
    private Symbol lastDeclaredSymbol = null;
    private boolean justDeclared = false; // tracks if last action was #11 (for init detection)

    private HashMap<Symbol, Object> symbolsValues = new HashMap<>();

    public void executeAction(int action, Token token) throws SemanticError
    {
        switch (action) {
            case 0,1,2,3,4,5,6,7,8,9: // push literal onto stack
            {
                if (justDeclared && lastDeclaredSymbol != null) {
                    lastDeclaredSymbol.isAlredyInitialized = true;
                    justDeclared = false;
                }
                String lexeme = token.getLexeme();
                SymbolType type = getSymbolTypeFromInteger(action);

                Object value = switch (type) {
                    case Integer -> Integer.parseInt(lexeme);
                    case Long -> Long.parseLong(
                            lexeme
                                    .replace("0b", "")
                                    .replace("0x", ""),
                            lexeme.startsWith("0b")
                                    ? 2
                                    : lexeme.startsWith("0x")
                                    ? 16
                                    : 10
                    );
                    case Float,
                         Double,
                         Decimal -> Double.parseDouble(lexeme);
                    case Boolean -> Boolean.parseBoolean(lexeme);
                    case Character -> lexeme.charAt(1);
                    case String -> lexeme.substring(
                            1,
                            lexeme.length() - 1
                    );
                    case Null -> null;
                    default -> lexeme;
                };

                literalStack.push(new Literal(type, value));
                break;
            }
            case 10: // capture variable type keyword
            {
                justDeclared = false;
                currentSymbolType = getSymbolTypeFromString(token.getLexeme());
                break;
            }
            case 11: // declare variable and insert into symbols table
            {
                String name = token.getLexeme();
                Symbol newSymbol = new Symbol(name, currentSymbolType, currentScope);
                insertIntoSymbolsTable(newSymbol);
                lastDeclaredSymbol = newSymbol;
                justDeclared = true;
                break;
            }
            case 22: // legacy — kept for safety, justDeclared handles init now
            {
                if (!literalStack.isEmpty()) literalStack.pop();
                break;
            }
            case 23: // declare parameter (same as #11 but sets isParameter = true)
            {
                String name = token.getLexeme();
                Symbol newSymbol = new Symbol(name, currentSymbolType, currentScope);
                newSymbol.isParameter = true;
                newSymbol.isAlredyInitialized = true; // parameters are always initialized by caller
                insertIntoSymbolsTable(newSymbol);
                lastDeclaredSymbol = newSymbol;
                break;
            }
            case 12: // read variable (access)
            {
                if (justDeclared && lastDeclaredSymbol != null) {
                    lastDeclaredSymbol.isAlredyInitialized = true;
                    justDeclared = false;
                }
                String name = token.getLexeme();
                Symbol sym = findSymbol(symbolsTable, name, currentScope);
                if (sym == null) {
                    throw new SemanticError("Symbol not found: " + name);
                }
                sym.isAlredyUsed = true;

                Object value = symbolsValues.get(sym);

                literalStack.push(new Literal(sym.type, value ));
                break;
            }
            case 13: // assign value to variable
            {
                Literal value = literalStack.pop();
                Literal target = literalStack.pop();
                Symbol sym = findSymbol(symbolsTable, (String) target.value, currentScope);

                if (sym == null) {
                    throw new SemanticError("Variable not declared: " + target.value);
                }

                boolean compatible = TypeCompatibility.isCompatible(OperationType.Equals, value.type, sym.type);
                if (!compatible) {
                    throw new SemanticError("Incompatible types: cannot assign " + value.type + " to " + sym.type);
                }

                symbolsValues.put(sym, value.value);
                sym.isAlredyInitialized = true;
                literalStack.push(new Literal(sym.type, target.value));
                break;
            }
            case 14: // capture left-hand side of assignment
            {
                String name = token.getLexeme();
                Symbol sym = findSymbol(symbolsTable, name, currentScope);
                if (sym == null) {
                    throw new SemanticError("Variable not declared: " + name);
                }
                literalStack.push(new Literal(sym.type, sym.id));
                break;
            }
            case 15: // open scope
            {
                String scopeName = "block_" + UUID.randomUUID().toString().substring(0, 6);
                Scope newScope = new Scope(scopeName, currentScope, false);
                scopeStack.push(newScope);
                currentScope = newScope;
                break;
            }
            case 16: // close scope
            {
                currentScope.isClosed = true;
                if (!scopeStack.isEmpty()) scopeStack.pop();
                currentScope = currentScope.parentScope;
                break;
            }
            case 17: // additive operator (+, -)
            {
                OperationType op = switch (token.getLexeme()) {
                    case "+" -> OperationType.Addition;
                    case "-" -> OperationType.Subtraction;
                    default -> throw new SemanticError("Unknown additive operator: " + token.getLexeme());
                };
                operatorStack.push(op);
                break;
            }
            case 18: // multiplicative operator (*, /, %)
            {
                OperationType op = switch (token.getLexeme()) {
                    case "*" -> OperationType.Multiplication;
                    case "/" -> OperationType.Division;
                    case "%" -> OperationType.Remainder;
                    default -> throw new SemanticError("Unknown multiplicative operator: " + token.getLexeme());
                };
                operatorStack.push(op);
                break;
            }
            case 19: // relational or equality operator
            {
                OperationType op = switch (token.getLexeme()) {
                    case ">"  -> OperationType.GreaterThan;
                    case "<"  -> OperationType.LessThan;
                    case ">=" -> OperationType.GreaterEqual;
                    case "<=" -> OperationType.LessEqual;
                    case "==" -> OperationType.Equality;
                    case "!=" -> OperationType.Inequality;
                    default -> throw new SemanticError("Unknown relational operator: " + token.getLexeme());
                };
                operatorStack.push(op);
                break;
            }
            case 20: // logical operator (&&, ||)
            {
                OperationType op = switch (token.getLexeme()) {
                    case "&&" -> OperationType.And;
                    case "||" -> OperationType.Or;
                    default -> throw new SemanticError("Unknown logical operator: " + token.getLexeme());
                };
                operatorStack.push(op);
                break;
            }
            case 21: // reduce multiplicative binary expression
            case 24: // reduce additive binary expression
            case 25: // reduce relational binary expression
            case 26: // reduce equality binary expression
            case 27: // reduce logical binary expression
            {
                Literal right = literalStack.pop();
                Literal left = literalStack.pop();

                OperationType op = operatorStack.pop();

                Boolean isLiteralCompatible = TypeCompatibility.isCompatible(op, left.type, right.type);
                if (!isLiteralCompatible) {
                    throw new SemanticError(
                            "Tipos incompatíveis para operação '" +
                                    op +
                                    "': " +
                                    left.type +
                                    " e " +
                                    right.type
                    );
                }

                Object result = null;
                try {
                    // Only evaluate if both literals have values
                    if (left.value != null && right.value != null) {

                        switch (op) {

                            // =========================
                            // LOGICAL
                            // =========================

                            case And -> {

                                boolean l = Boolean.parseBoolean(left.value.toString());
                                boolean r = Boolean.parseBoolean(right.value.toString());

                                result = l && r;
                            }

                            case Or -> {

                                boolean l = Boolean.parseBoolean(left.value.toString());
                                boolean r = Boolean.parseBoolean(right.value.toString());

                                result = l || r;
                            }

                            // =========================
                            // RELATIONAL
                            // =========================

                            case GreaterThan -> {

                                double l = Double.parseDouble(left.value.toString());
                                double r = Double.parseDouble(right.value.toString());

                                result = l > r;
                            }

                            case LessThan -> {

                                double l = Double.parseDouble(left.value.toString());
                                double r = Double.parseDouble(right.value.toString());

                                result = l < r;
                            }

                            case GreaterEqual -> {

                                double l = Double.parseDouble(left.value.toString());
                                double r = Double.parseDouble(right.value.toString());

                                result = l >= r;
                            }

                            case LessEqual -> {

                                double l = Double.parseDouble(left.value.toString());
                                double r = Double.parseDouble(right.value.toString());

                                result = l <= r;
                            }

                            // =========================
                            // EQUALITY
                            // =========================

                            case Equality -> {

                                result = Objects.equals(
                                        left.value,
                                        right.value
                                );
                            }

                            case Inequality -> {

                                result = !Objects.equals(
                                        left.value,
                                        right.value
                                );
                            }

                            // =========================
                            // ARITHMETIC
                            // =========================

                            case Addition -> {

                                double l = Double.parseDouble(left.value.toString());
                                double r = Double.parseDouble(right.value.toString());

                                result = l + r;
                            }

                            case Subtraction -> {

                                double l = Double.parseDouble(left.value.toString());
                                double r = Double.parseDouble(right.value.toString());

                                result = l - r;
                            }

                            case Multiplication -> {

                                double l = Double.parseDouble(left.value.toString());
                                double r = Double.parseDouble(right.value.toString());

                                result = l * r;
                            }

                            case Division -> {

                                double l = Double.parseDouble(left.value.toString());
                                double r = Double.parseDouble(right.value.toString());

                                if (r == 0) {
                                    throw new SemanticError(
                                            "Divisão por zero"
                                    );
                                }

                                result = l / r;
                            }

                            case Remainder -> {

                                double l = Double.parseDouble(left.value.toString());
                                double r = Double.parseDouble(right.value.toString());

                                result = l % r;
                            }

                            // =========================
                            // BITWISE
                            // =========================

                            case BitAnd -> {

                                int l = Integer.parseInt(left.value.toString());
                                int r = Integer.parseInt(right.value.toString());

                                result = l & r;
                            }

                            case BitOr -> {

                                int l = Integer.parseInt(left.value.toString());
                                int r = Integer.parseInt(right.value.toString());

                                result = l | r;
                            }

                            case BitXor -> {

                                int l = Integer.parseInt(left.value.toString());
                                int r = Integer.parseInt(right.value.toString());

                                result = l ^ r;
                            }

                            case BitShiftLeft -> {

                                int l = Integer.parseInt(left.value.toString());
                                int r = Integer.parseInt(right.value.toString());

                                result = l << r;
                            }

                            case BitShiftRight -> {

                                int l = Integer.parseInt(left.value.toString());
                                int r = Integer.parseInt(right.value.toString());

                                result = l >> r;
                            }

                            default -> {
                                // unsupported folding
                                result = null;
                            }
                        }
                    }

                } catch (NumberFormatException ex) {

                    throw new SemanticError(
                            "Erro ao avaliar expressão constante"
                    );
                }

                SymbolType resultType =
                        TypeCompatibility.resultType(
                                op,
                                left.type,
                                right.type
                        );

                literalStack.push(
                        new Literal(
                                resultType,
                                result != null
                                        ? result.toString()
                                        : null
                        )
                );

                break;
            }
            default:
                throw new SemanticError("Semantico: Invalid action " + action);
        }
    }

    // ----------------------------------------------------------------
    // Public getters for IDE display
    // ----------------------------------------------------------------

    public List<Symbol> getSymbolsTable() {
        return Collections.unmodifiableList(symbolsTable);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Returns symbol table rows for JTable.
     *
     * Columns:
     * [
     *   nome,
     *   tipo,
     *   escopo,
     *   inicializado,
     *   usado,
     *   parametro,
     *   posicaoParametro,
     *   array,
     *   matriz,
     *   porReferencia,
     *   funcao
     * ]
     */
    public List<Object[]> getSymbolTableRows() {

        List<Object[]> rows = new ArrayList<>();

        for (Symbol s : symbolsTable) {

            rows.add(new Object[]{

                    // Nome
                    s.id,

                    // Tipo
                    s.type != null
                            ? s.type.name()
                            : "?",

                    // Escopo
                    s.scope != null
                            ? s.scope.name
                            : "global",

                    // Inicializado
                    Boolean.TRUE.equals(s.isAlredyInitialized)
                            ? "Sim"
                            : "Não",

                    // Usado
                    Boolean.TRUE.equals(s.isAlredyUsed)
                            ? "Sim"
                            : "Não",

                    // Parâmetro
                    Boolean.TRUE.equals(s.isParameter)
                            ? "Sim"
                            : "Não",

                    // Posição do parâmetro
                    s.paramterPosition != null
                            ? s.paramterPosition
                            : "-",

                    // Array
                    Boolean.TRUE.equals(s.isArray)
                            ? "Sim"
                            : "Não",

                    // Matriz
                    Boolean.TRUE.equals(s.isMatrix)
                            ? "Sim"
                            : "Não",

                    // Por referência
                    Boolean.TRUE.equals(s.isByReference)
                            ? "Sim"
                            : "Não",

                    // Função
                    Boolean.TRUE.equals(s.isFunction)
                            ? "Sim"
                            : "Não"
            });
        }

        return rows;
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private SymbolType getSymbolTypeFromInteger(int action) {
        return switch (action) {
            case 0    -> SymbolType.Integer;   // number
            case 2    -> SymbolType.Long;      // binary_number
            case 3    -> SymbolType.Long;      // hex_number
            case 4    -> SymbolType.Float;     // real_number
            case 5    -> SymbolType.Character; // char_literal
            case 6    -> SymbolType.String;    // string_literal
            case 7, 8 -> SymbolType.Boolean;   // true, false
            case 9    -> SymbolType.Null;      // null
            default   -> throw new IllegalStateException("Unexpected literal action: " + action);
        };
    }

    private SymbolType getSymbolTypeFromString(String type) {
        return switch (type) {
            case "char"    -> SymbolType.Character;
            case "string"  -> SymbolType.String;
            case "bool"    -> SymbolType.Boolean;
            case "short"   -> SymbolType.Short;
            case "int"     -> SymbolType.Integer;
            case "long"    -> SymbolType.Long;
            case "float"   -> SymbolType.Float;
            case "double"  -> SymbolType.Double;
            case "decimal" -> SymbolType.Decimal;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    private Symbol findSymbol(List<Symbol> table, String id, Scope scope) {
        while (scope != null) {
            Scope finalScope = scope;
            Optional<Symbol> found = table.stream()
                    .filter(s -> Objects.equals(s.id, id) && s.scope == finalScope)
                    .findFirst();
            if (found.isPresent()) return found.get();
            scope = scope.parentScope;
        }
        return null;
    }

    private void insertIntoSymbolsTable(Symbol sym) throws SemanticError {
        boolean exists = symbolsTable.stream()
                .anyMatch(s -> Objects.equals(s.id, sym.id) && Objects.equals(s.scope, sym.scope));
        if (exists) {
            throw new SemanticError("Identificador já declarado neste escopo: " + sym.id);
        }
        symbolsTable.add(sym);
    }

    public void generateWarnings() {
        // Gera avisos para simbolos não utilizados
        List<Symbol> unusedSymbols = symbolsTable
                .stream()
                .filter(
                        symbol -> symbol.isAlredyUsed == false
                ).toList();

        for (Symbol symbol : unusedSymbols) {
            String message = String.format("O símbolo '%s' não é utilizado!", symbol.id);
            warnings.add(message);
        }

        // Gera avisos para simbolos utilizados mas não inicializados
        List<Symbol> usedUnitilizedSymbols = symbolsTable
                .stream()
                .filter(
                        symbol -> symbol.isAlredyUsed == true && symbol.isAlredyInitialized == false
                ).toList();

        for (Symbol symbol : usedUnitilizedSymbols) {
            String message = String.format("O símbolo '%s' é utilizado sem ser inicializado!", symbol.id);
            warnings.add(message);
        }
    }
}

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


    public void executeAction(int action, Token token) throws SemanticError
    {
        switch (action) {
            case 0,1,2,3,4,5,6,7,8,9: // push literal onto stack
            {
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
                    case String -> lexeme.substring(1, lexeme.length() - 1);
                    case Null -> null;
                    default -> lexeme;
                };

                if (justDeclared && lastDeclaredSymbol != null) {
                    lastDeclaredSymbol.isAlredyInitialized = true;
                    lastDeclaredSymbol.value = value;
                    justDeclared = false;
                }

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
                String name = token.getLexeme();
                Symbol sym = findSymbol(symbolsTable, name, currentScope);
                if (sym == null) {
                    throw new SemanticError("Symbol not found: " + name);
                }
                if (justDeclared && lastDeclaredSymbol != null) {
                    lastDeclaredSymbol.isAlredyInitialized = true;
                    lastDeclaredSymbol.value = sym.value;
                    justDeclared = false;
                }
                sym.isAlredyUsed = true;
                literalStack.push(new Literal(sym.type, sym.value));
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

                sym.value = value.value;
                sym.isAlredyInitialized = true;
                literalStack.push(new Literal(sym.type, sym.value));
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

                            case And -> result = toBool(left.value) && toBool(right.value);

                            case Or -> result = toBool(left.value) || toBool(right.value);

                            // =========================
                            // RELATIONAL
                            // =========================

                            case GreaterThan  -> result = toDouble(left.value) > toDouble(right.value);
                            case LessThan     -> result = toDouble(left.value) < toDouble(right.value);
                            case GreaterEqual -> result = toDouble(left.value) >= toDouble(right.value);
                            case LessEqual    -> result = toDouble(left.value) <= toDouble(right.value);

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
                                if (left.type == SymbolType.String || right.type == SymbolType.String) {
                                    result = left.value.toString() + right.value.toString();
                                } else {
                                    result = toDouble(left.value) + toDouble(right.value);
                                }
                            }

                            case Subtraction -> result = toDouble(left.value) - toDouble(right.value);

                            case Multiplication -> result = toDouble(left.value) * toDouble(right.value);

                            case Division -> {
                                double r = toDouble(right.value);
                                if (r == 0) throw new SemanticError("Divisão por zero");
                                result = toDouble(left.value) / r;
                            }

                            case Remainder -> result = toDouble(left.value) % toDouble(right.value);

                            // =========================
                            // BITWISE
                            // =========================

                            case BitAnd       -> result = toInt(left.value) & toInt(right.value);
                            case BitOr        -> result = toInt(left.value) | toInt(right.value);
                            case BitXor       -> result = toInt(left.value) ^ toInt(right.value);
                            case BitShiftLeft  -> result = toInt(left.value) << toInt(right.value);
                            case BitShiftRight -> result = toInt(left.value) >> toInt(right.value);

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

                SymbolType resultType = TypeCompatibility.resultType(op, left.type, right.type);
                literalStack.push(new Literal(resultType, coerce(result, resultType)));

                break;
            }
            case 28: // mark last declared symbol as array (fires after close_bracket in declarator/parameter)
            {
                if (lastDeclaredSymbol != null) {
                    lastDeclaredSymbol.isArray = true;
                }
                break;
            }
            case 29: // declare function and insert into symbols table with isFunction = true
            {
                String name = token.getLexeme();
                Symbol newSymbol = new Symbol(name, currentSymbolType, currentScope);
                newSymbol.isFunction = true;
                newSymbol.isAlredyInitialized = true;
                insertIntoSymbolsTable(newSymbol);
                lastDeclaredSymbol = newSymbol;
                justDeclared = false;
                break;
            }
            case 30: // void keyword — set currentSymbolType to Void
            {
                justDeclared = false;
                currentSymbolType = SymbolType.Void;
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
     * [0] nome
     * [1] tipo
     * [2] escopo
     * [3] inicializado
     * [4] usado
     * [5] parametro
     * [6] posicaoParametro
     * [7] array
     * [8] matriz
     * [9] porReferencia
     * [10] funcao
     * [11] valor (Object, null se não inicializado)
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
                            : "Não",

                    // Valor
                    s.value
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

    private double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }

    private int toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(v.toString());
    }

    private boolean toBool(Object v) {
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }

    private Object coerce(Object val, SymbolType type) {
        if (val == null) return null;
        if (!(val instanceof Number n)) return val;
        return switch (type) {
            case Short, Integer -> n.intValue();
            case Long           -> n.longValue();
            case Float, Double, Decimal -> n.doubleValue();
            default -> val;
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

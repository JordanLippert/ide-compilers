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

    public void executeAction(int action, Token token) throws SemanticError
    {
        switch (action) {
            case 0,1,2,3,4,5,6,7,8,9: // push literal onto stack
            {
                String literalName = token.getLexeme();
                SymbolType type = getSymbolTypeFromInteger(action);
                literalStack.push(new Literal(type, literalName));
                break;
            }
            case 10: // capture variable type keyword
            {
                currentSymbolType = getSymbolTypeFromString(token.getLexeme());
                break;
            }
            case 11: // declare variable and insert into symbols table
            {
                String name = token.getLexeme();
                Symbol newSymbol = new Symbol(name, currentSymbolType, currentScope);
                insertIntoSymbolsTable(newSymbol);
                break;
            }
            case 12: // read variable (access)
            {
                String name = token.getLexeme();
                Symbol sym = findSymbol(symbolsTable, name, currentScope);
                if (sym == null) {
                    throw new SemanticError("Symbol not found: " + name);
                }
                if (!sym.isAlredyInitialized) {
                    warnings.add("Aviso: variável '" + name + "' usada sem inicialização (possível lixo de memória)");
                }
                sym.isAlredyUsed = true;
                literalStack.push(new Literal(sym.type, sym.id));
                break;
            }
            case 13: // assign value to variable
            {
                Literal value = literalStack.pop();
                Literal target = literalStack.pop();
                Symbol sym = findSymbol(symbolsTable, target.value, currentScope);

                if (sym == null) {
                    throw new SemanticError("Variable not declared: " + target.value);
                }

                boolean compatible = TypeCompatibility.isCompatible(OperationType.Equals, value.type, sym.type);
                if (!compatible) {
                    throw new SemanticError("Incompatible types: cannot assign " + value.type + " to " + sym.type);
                }

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
            case 16: // close scope — warn about unused variables
            {
                for (Symbol sym : symbolsTable) {
                    if (sym.scope == currentScope && !sym.isAlredyUsed) {
                        warnings.add("Aviso: variável '" + sym.id + "' declarada mas nunca usada");
                    }
                }
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
            case 21: // reduce binary expression — type check
            {
                Literal right = literalStack.pop();
                Literal left = literalStack.pop();
                OperationType op = operatorStack.pop();

                if (!TypeCompatibility.isCompatible(op, left.type, right.type)) {
                    throw new SemanticError("Tipos incompatíveis para operação '" + op + "': " + left.type + " e " + right.type);
                }

                SymbolType resultType = TypeCompatibility.resultType(op, left.type, right.type);
                literalStack.push(new Literal(resultType, null));
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

    /** Returns symbol table as rows for JTable: [name, type, scope, initialized, used] */
    public List<Object[]> getSymbolTableRows() {
        List<Object[]> rows = new ArrayList<>();
        for (Symbol s : symbolsTable) {
            rows.add(new Object[]{
                s.id,
                s.type != null ? s.type.name() : "?",
                s.scope != null ? s.scope.name : "global",
                s.isAlredyInitialized ? "Sim" : "Não",
                s.isAlredyUsed      ? "Sim" : "Não"
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
}

package compiler.gals;

import java.util.*;

public class Semantico implements Constants
{
    private final Stack<Literal> literalStack = new Stack<>();
    private final Stack<Object> scopeStack = new Stack<>();
    private final Stack<Integer> operatorStack = new Stack<>();

    private final List<Symbol> symbolsTable = new ArrayList<>();
    private final List<Scope> scopesTable = new ArrayList<>();

    private SymbolType currentSymbolType = null;
    private Scope currentScope = new Scope("global", null);
    private Symbol currentSymbol = null;

    // private final HashMap<(SymbolType, SymbolType), List<OperationType>> operationCompatibilityByTypeTable

    public void executeAction(int action, Token token)	throws SemanticError
    {
        switch (action) {
            case 0,1,2,3,4,5,6,7,8,9: // add literal to stack
            {
                String literalName = token.getLexeme();
                SymbolType type = getSymbolTypeFromInteger(action);
                Literal literal = new Literal(type, literalName);
                literalStack.push(literal);
                break;
            }
            case 10: // get variable type
            {
                String type = token.getLexeme();
                currentSymbolType = getSymbolTypeFromString(type);
                break;
            }
            case 11: // get variable name and save in symbols table
            {
                String name = token.getLexeme();
                Symbol newSymbol = new Symbol(name, currentSymbolType, currentScope);
                try
                {
                    insertIntoSymbolsTable(newSymbol);
                }
                catch (SemanticError e) {
                    throw e;
                }
                break;
            }
            case 12: // access a variable
            {
                String accessedSymbolName = token.getLexeme();
                Symbol accessedSymbol = findSymbol(symbolsTable, accessedSymbolName, currentScope);
                if (Objects.isNull(accessedSymbol)) {
                    throw new SemanticError("Symbol not found: " + accessedSymbolName);
                }

                literalStack.push(new Literal(accessedSymbol.type, accessedSymbol.id));
                break;
            }
            case 13: // assign variable
            {
                Literal value = literalStack.pop();
                Literal target = literalStack.pop();
                Symbol symbol = findSymbol(symbolsTable, target.value, currentScope);

                if (symbol != null) {
                    SemanticTable.Result result = SemanticTable.atribType(symbol.type, value.type);

                    if (result == SemanticTable.Result.ERROR) {
                        throw new SemanticError("Type mismatch: cannot assign " + value.type + " to " + symbol.type);
                    }

                    if (result == SemanticTable.Result.WARNING) {
                        System.out.println("Warning: implicit conversion from " + value.type + " to " + symbol.type);
                    }

                    symbol.isAlredyInitialized = true;
                }

                literalStack.push(new Literal(symbol.type, target.value));
                break;
            }
            case 14: // captura variável do lado esquerdo do assignment
            {
                String variableName = token.getLexeme();
                Symbol symbol = findSymbol(symbolsTable, variableName, currentScope);

                if (symbol == null) {
                    throw new SemanticError("Variable not declared: " + variableName);
                }
                literalStack.push(new Literal(symbol.type, symbol.id));
                break;
            }
            case 15: // entrar escopo
            {
                String scopeName = "block_" + scopesTable.size();
                Scope newScope = new Scope(scopeName, currentScope);
                scopesTable.add(newScope);
                scopeStack.push(newScope);
                currentScope = newScope;
                break;
            }
            case 16: // sair escopo
            {
                scopeStack.pop();
                currentScope = currentScope.parentScope;
                break;
            }
            case 17: // categoria aritmética
            {
                operatorStack.push(0);
                break;
            }
            case 18: // mesma catergoria aritmética
            {
                operatorStack.push(0);
                break;
            }
            case 19: // categoria relacional
            {
                operatorStack.push(1);
                break;
            }
            case 20: // categoria lógica
            {
                operatorStack.push(2);
                break;
            }
            case 21: {
                Literal right = literalStack.pop();
                Literal left = literalStack.pop();
                int opCategory = operatorStack.pop();
                SymbolType result = SemanticTable.resultType(left.type, right.type, opCategory);

                if (result == null) {
                    throw new SemanticError("Incompatible types: " + left.type + " and " + right.type);
                }

                literalStack.push(new Literal(result, null));
                break;
            }
            default:
                throw new SemanticError("Semantico: Invalid action " + action);
        }
    }

    private SymbolType getSymbolTypeFromInteger(int type) {
        return switch (type) {
            case 0 -> SymbolType.Integer;   // number
            case 2 -> SymbolType.Long;      // binary_number
            case 3 -> SymbolType.Long;      // hex_number
            case 4 -> SymbolType.Float;     // real_number
            case 5 -> SymbolType.Character; // char_literal
            case 6 -> SymbolType.String;    // string_literal
            case 7, 8 -> SymbolType.Boolean; // true, false
            case 9 -> SymbolType.Null;      // null
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private Symbol findSymbol(List<Symbol> table, String id, Scope currentScope) {
        Scope scope = currentScope;

        while (scope != null) {
            Scope finalScope = scope;

            Optional<Symbol> found = table.stream()
                    .filter(s -> Objects.equals(s.id, id) && s.scope == finalScope)
                    .findFirst();

            if (found.isPresent()) {
                return found.get();
            }

            scope = scope.parentScope;
        }

        return null;
    }

    private void insertIntoSymbolsTable(Symbol symbolToInsert) throws SemanticError {
        // Verifica se o mesmo id já existe no mesmo escopo
        boolean isSymbolInSymbolsTable = symbolsTable
                .stream()
                .anyMatch(symbol -> Objects.equals(symbol.id, symbolToInsert.id) && Objects.equals(symbol.scope, symbolToInsert.scope));

        if (isSymbolInSymbolsTable) {
            throw new SemanticError("Symbol already exists in symbols table");
        }

        symbolsTable.add(symbolToInsert);
    }

    private SymbolType getSymbolTypeFromString(String type) {
        return switch (type) {
            case "char" -> SymbolType.Character;
            case "string" -> SymbolType.String;
            case "bool" -> SymbolType.Boolean;
            case "short" -> SymbolType.Short;
            case "int" -> SymbolType.Integer;
            case "long" -> SymbolType.Long;
            case "float" -> SymbolType.Float;
            case "double" -> SymbolType.Double;
            case "decimal" -> SymbolType.Decimal;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    // TODO: function to reduce operation
//    private void reduceBinary(OperationType op) throws SemanticError {
//        Literal right = literalStack.pop();
//        Literal left = literalStack.pop();
//
//        // type checking (simplified)
//        SymbolType resultType = checkCompatibility(left.type, right.type, op);
//
//        Literal result = new Literal(resultType, null); // value optional in semantic phase
//        literalStack.push (result);
//    }

    // TODO: function to reduce assigment operation
//    private void reduceAssignment() throws SemanticError {
//        Literal value = literalStack.pop();   // result of expression
//        Literal target = literalStack.pop();  // variable
//
//        Symbol symbol = findSymbol(symbolsTable, target.value, currentScope);
//
//        if (symbol == null) {
//            throw new SemanticError("Variable not declared: " + target.value);
//        }
//
//        // type check
//        if (!isAssignable(symbol.type, value.type)) {
//            throw new SemanticError("Type mismatch in assignment");
//        }
//
//        symbol.isAlredyInitialized = true;
//
//        // push result (optional, depends on your language semantics)
//        literalStack.push(new Literal(symbol.type, target.value));
//    }
}
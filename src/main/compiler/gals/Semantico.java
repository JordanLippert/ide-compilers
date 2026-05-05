package compiler.gals;

import java.util.*;

public class Semantico implements Constants
{
    private final Stack<Literal> literalStack = new Stack<>();
    private final Stack<Object> scopeStack = new Stack<>();

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
                    System.out.println(e);
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
                // TODO
            }
            default:
                throw new SemanticError("Semantico: Invalid action " + action);
        }
    }

    private SymbolType getSymbolTypeFromInteger(int type) {
        return switch (type) {
            case 0 -> SymbolType.Integer; // number
            case 1 -> SymbolType.Long; // binary number
            case 2 -> SymbolType.Long; // hex number
            case 3 -> SymbolType.Float; // real number
            case 4 -> SymbolType.Character; // char
            case 5 -> SymbolType.String; // string
            case 6, 7 -> SymbolType.Boolean;
            case 8 -> SymbolType.Null; // null
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
package compiler.gals;

import java.util.*;

public class Semantico implements Constants
{
    private final Stack<Object> stack = new Stack<>();
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
            case 1: // add literal to stack
            {
                String literal = token.getLexeme();
                stack.push(literal);
                break;
            }
            case 2: // get variable type
            {
                String type = token.getLexeme();
                currentSymbolType = getSymbolTypeFromString(type);
                break;
            }
            case 3: // get variable name and save in symbols table
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
            case 4: // access a variable
            {
                String accessedSymbolName = token.getLexeme();
                Symbol accessedSymbol = findSymbol(symbolsTable, accessedSymbolName, currentScope);
                if (Objects.isNull(accessedSymbol)) {
                    throw new SemanticError("Symbol not found: " + accessedSymbolName);
                }
                break;
            }
            default:
                throw new SemanticError("Semantico: Invalid action " + action);
        }
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
}
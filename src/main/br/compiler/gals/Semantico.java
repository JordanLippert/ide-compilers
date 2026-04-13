package br.compiler.gals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class Semantico implements Constants
{
    private final Stack<Object> stack = new Stack<>();

    // Variables
    private final HashMap<String, Variable> variables = new HashMap<>();
    private Type currentVariableType;
    private String currentVariableName;

    // Functions
    private Stack<String> argumnents = new Stack<>();
    private final HashMap<String, Function> functions = new HashMap<>();
    private String currentFunctionName;

    // Arrays
    private boolean isArray = false;
    private Integer arraySize;

    public void executeAction(int action, Token token)	throws SemanticError
    {
        switch (action) {
            case 0: // clear context
            {
                currentVariableName = null;
                currentVariableType = null;
                isArray = false;
                arraySize = null;
                break;
            }
            case 1: // push literal to stack
            {
                Object literal = token.getLexeme();
                stack.push(literal);
                break;
            }
            case 2: // get variable/function type
            {
                currentVariableType = Type.FromString(token.getLexeme());
                break;
            }
            case 3: // get name of the variable
            {
                currentVariableName = token.getLexeme();
                break;
            }
            case 4: // add variable to variables map
            {
                Variable var = new Variable(currentVariableType, null, isArray);

                if (isArray && arraySize != null) {
                    var.setArraySize(arraySize);
                }

                variables.put(currentVariableName, var);
                break;
            }
            case 5: // store function name
            {
                currentFunctionName = token.getLexeme();
                break;
            }
            case 6: // store function arguments
            {
                String argument = token.getLexeme();
                argumnents.push(argument);
                break;
            }
            case 7: // function call
            {
                List<Object> args = new ArrayList<>();

                while (!argumnents.isEmpty()) {
                    String argument = argumnents.pop();
                    Object value = variables.get(argument).getValue();
                    args.add(0, value);
                }

                Function function = functions.get(currentFunctionName);

                if (function == null)
                    throw new SemanticError("Function not found: " + currentFunctionName);

                if (args.size() != function.numberOfParameters)
                    throw new SemanticError("Invalid number of arguments");

                Object result = function.execute(args);

                if (result != null)
                    stack.push(result);

                break;
            }
            case 8: // initialize variable value
            {
                if (currentVariableName == null)
                    throw new SemanticError("No variable selected");

                Object rawValue = stack.pop();

                Object typedValue = convertToType(rawValue, currentVariableType);

                variables.get(currentVariableName).setValue(typedValue);
                break;
            }
            case 9: // assign value to variable
            {
                if (currentVariableName == null)
                    throw new SemanticError("No variable selected");

                Variable variable = variables.get(currentVariableName);

                Object rawValue = stack.pop();
                Object typedValue = convertToType(rawValue, variable.getType());

                variable.setValue(typedValue);
                break;
            }
            case 10: // array with size
            {
                isArray = true;
                Object size = stack.pop();
                arraySize = Integer.parseInt(size.toString());
                break;
            }
            case 11: // array without size
            {
                isArray = true;
                arraySize = null;
                break;
            }
            case 12: // array assignment
            {
                Object value = stack.pop();   // value
                Object indexObj = stack.pop(); // index

                int index = Integer.parseInt(indexObj.toString());

                Variable var = variables.get(currentVariableName);

                if (!var.isArray())
                    throw new SemanticError("Not an array");

                var.setArrayValue(index, convertToType(value, var.getType()));
                break;
            }
            default:
                throw new SemanticError("Semantico: Invalid action " + action);
        }
    }

    private Object convertToType(Object value, Type type) throws SemanticError {

        try {
            return switch (type) {
                case Type.INT -> (int) Double.parseDouble(value.toString());
                case Type.DOUBLE, Type.FLOAT -> Double.parseDouble(value.toString());
                case Type.BOOL -> toBoolean(value);
                case Type.CHAR -> value.toString().charAt(0);
                case Type.STRING -> value.toString();
                default -> throw new SemanticError("Unknown type: " + type);
            };
        } catch (Exception e) {
            throw new SemanticError("Type conversion error: " + value + " -> " + type);
        }
    }

    private boolean toBoolean(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.doubleValue() != 0;
        return Boolean.parseBoolean(o.toString());
    }
}
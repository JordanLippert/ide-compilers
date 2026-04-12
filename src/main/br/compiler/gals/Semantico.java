package br.compiler.gals;

import java.util.*;

public class Semantico implements Constants {

    private final HashMap<String, Object> variables = new HashMap<>();
    private final Stack<Object> stack = new Stack<>();

    private final HashMap<String, Function> functions = new HashMap<>();
    private final List<String> currentParams = new ArrayList<>();

    private String currentVar;

    private final Stack<Boolean> executionStack = new Stack<>();

    private boolean isExecuting() {
        return executionStack.isEmpty() || executionStack.peek();
    }

    // 🔥 SAFE STACK OPS
    private Object safePop() {
        return stack.isEmpty() ? 0.0 : stack.pop();
    }

    private void pushDummy() {
        stack.push(0.0);
    }

    public void executeAction(int action, Token token) throws SemanticError {

        switch (action) {

            // =========================
            // VARIABLES / ACCESS
            // =========================

            case 1: {
                if (!isExecuting()) {
                    pushDummy();
                    break;
                }

                String name = token.getLexeme();

                if (!variables.containsKey(name))
                    throw new SemanticError("Variable not declared: " + name);

                stack.push(variables.get(name));
                break;
            }

            case 2: {
                Object indexObj = safePop();

                if (!isExecuting()) {
                    pushDummy();
                    break;
                }

                int index = (int) toDouble(indexObj);
                String name = token.getLexeme();

                Object arr = variables.get(name);

                if (!(arr instanceof Object[]))
                    throw new SemanticError("Not a vector: " + name);

                stack.push(((Object[]) arr)[index]);
                break;
            }

            // =========================
            // DECLARATION
            // =========================

            case 3: {
                if (!isExecuting()) break;
                currentVar = token.getLexeme();
                variables.put(currentVar, null);
                break;
            }

            case 4: {
                Object value = safePop();
                if (!isExecuting()) break;
                variables.put(currentVar, value);
                break;
            }

            case 5: {
                Object value = safePop();
                if (!isExecuting()) break;

                if (value instanceof String str) {
                    Character[] arr = new Character[str.length() + 1];
                    for (int i = 0; i < str.length(); i++)
                        arr[i] = str.charAt(i);
                    arr[str.length()] = '\0';
                    variables.put(currentVar, arr);
                    break;
                }

                if (value instanceof List<?> list) {
                    variables.put(currentVar, list.toArray());
                    break;
                }

                variables.put(currentVar, new Object[0]);
                break;
            }

            // =========================
            // IO
            // =========================

            case 6: {
                if (!isExecuting()) {
                    safePop();
                    break;
                }

                Scanner sc = new Scanner(System.in);
                Object var = safePop();

                if (var instanceof String name && variables.containsKey(name)) {
                    System.out.print("Input for " + name + ": ");
                    variables.put(name, sc.nextDouble());
                }
                break;
            }

            case 7: {
                Object val = safePop();
                if (!isExecuting()) break;
                System.out.println(val);
                break;
            }

            case 8: {
                Object val = safePop();
                if (!isExecuting()) break;
                System.out.println("RETURN: " + val);
                break;
            }

            // =========================
            // LITERALS
            // =========================

            case 9: stack.push(Double.parseDouble(token.getLexeme())); break;
            case 10: stack.push(token.getLexeme().charAt(0)); break;
            case 11: stack.push(token.getLexeme()); break;
            case 12: stack.push(Boolean.parseBoolean(token.getLexeme())); break;
            case 13: stack.push(null); break;

            // =========================
            // ARITHMETIC
            // =========================

            case 14: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { pushDummy(); break; }
                stack.push(toDouble(b) + toDouble(a));
                break;
            }

            case 15: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { pushDummy(); break; }
                stack.push(toDouble(b) - toDouble(a));
                break;
            }

            case 16: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { pushDummy(); break; }
                stack.push(toDouble(b) * toDouble(a));
                break;
            }

            case 17: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { pushDummy(); break; }
                stack.push(toDouble(b) / toDouble(a));
                break;
            }

            case 18: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { pushDummy(); break; }
                stack.push(toDouble(b) % toDouble(a));
                break;
            }

            // =========================
            // RELATIONAL
            // =========================

            case 19: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { stack.push(false); break; }
                stack.push(toDouble(b) > toDouble(a));
                break;
            }

            case 20: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { stack.push(false); break; }
                stack.push(toDouble(b) < toDouble(a));
                break;
            }

            case 21: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { stack.push(false); break; }
                stack.push(toDouble(b) >= toDouble(a));
                break;
            }

            case 22: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { stack.push(false); break; }
                stack.push(toDouble(b) <= toDouble(a));
                break;
            }

            // =========================
            // LOGICAL
            // =========================

            case 23: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { stack.push(false); break; }
                stack.push((Boolean) b && (Boolean) a);
                break;
            }

            case 24: {
                Object a = safePop(), b = safePop();
                if (!isExecuting()) { stack.push(false); break; }
                stack.push((Boolean) b || (Boolean) a);
                break;
            }

            // =========================
            // UNARY
            // =========================

            case 25: {
                Object a = safePop();
                if (!isExecuting()) { stack.push(false); break; }
                stack.push(!(Boolean) a);
                break;
            }

            case 26: {
                Object a = safePop();
                if (!isExecuting()) { pushDummy(); break; }
                stack.push(~(int) toDouble(a));
                break;
            }

            case 27: break;

            case 28: {
                Object a = safePop();
                if (!isExecuting()) { pushDummy(); break; }
                stack.push(-toDouble(a));
                break;
            }

            case 29: {
                if (!isExecuting()) break;
                String name = token.getLexeme();
                variables.put(name, toDouble(variables.get(name)) + 1);
                break;
            }

            case 30: {
                if (!isExecuting()) break;
                String name = token.getLexeme();
                variables.put(name, toDouble(variables.get(name)) - 1);
                break;
            }

            // =========================
            // FUNCTIONS
            // =========================

            case 31: stack.push(null); break;

            case 32: {
                String name = token.getLexeme();
                if (!functions.containsKey(name))
                    throw new SemanticError("Function not declared: " + name);

                Function f = functions.get(name);
                for (int i = 0; i < f.params.size(); i++) safePop();

                stack.push(null);
                break;
            }

            case 33:
            case 34: {
                functions.put(token.getLexeme(),
                        new Function(token.getLexeme(), new ArrayList<>(currentParams), action == 34));
                currentParams.clear();
                break;
            }

            case 35: currentParams.add(token.getLexeme()); break;

            case 36: {
                Object value = safePop();
                if (!isExecuting()) break;

                String name = token.getLexeme();
                if (!variables.containsKey(name))
                    throw new SemanticError("Variable not declared: " + name);

                variables.put(name, value);
                break;
            }

            // =========================
            // CONTROL FLOW
            // =========================

            case 40: {
                boolean cond = toBoolean(safePop());

                if (!isExecuting())
                    executionStack.push(false);
                else
                    executionStack.push(cond);

                break;
            }

            case 41: {
                boolean last = executionStack.pop();

                if (!isExecuting())
                    executionStack.push(false);
                else
                    executionStack.push(!last);

                break;
            }

            case 46: {
                if (!executionStack.isEmpty())
                    executionStack.pop();
                break;
            }

            // =========================
            // VECTOR INIT
            // =========================

            case 50: {
                List<Object> list = new ArrayList<>();
                list.add(safePop());
                stack.push(list);
                break;
            }

            case 51: {
                Object value = safePop();
                List<Object> list = (List<Object>) safePop();
                list.add(value);
                stack.push(list);
                break;
            }

            default:
                throw new SemanticError("Unknown action: " + action);
        }
    }

    private boolean toBoolean(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.doubleValue() != 0;
        if (o == null) return false;
        return Boolean.parseBoolean(o.toString());
    }

    private double toDouble(Object o) {
        return Double.parseDouble(o.toString());
    }
}
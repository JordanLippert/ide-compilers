package compiler.gals;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TypeCompatibility {

    /**
     * operation -> (leftType -> compatible right types)
     */
    private static final Map<OperationType, Map<SymbolType, Set<SymbolType>>> COMPATIBILITY =
            new EnumMap<>(OperationType.class);

    /**
     * Implicit numeric promotion table (similar to C#)
     *
     * short  -> int
     * int    -> long
     * long   -> float
     * float  -> double
     * double -> decimal
     */
    private static final Map<SymbolType, Integer> NUMERIC_RANK =
            new EnumMap<>(SymbolType.class);

    static {

        // =========================================
        // Numeric promotion hierarchy
        // =========================================

        NUMERIC_RANK.put(SymbolType.Short, 1);
        NUMERIC_RANK.put(SymbolType.Integer, 2);
        NUMERIC_RANK.put(SymbolType.Long, 3);
        NUMERIC_RANK.put(SymbolType.Float, 4);
        NUMERIC_RANK.put(SymbolType.Double, 5);
        NUMERIC_RANK.put(SymbolType.Decimal, 6);

        // =========================================
        // Arithmetic
        // =========================================

        registerNumeric(OperationType.Addition);
        registerNumeric(OperationType.Subtraction);
        registerNumeric(OperationType.Multiplication);
        registerNumeric(OperationType.Division);
        registerNumeric(OperationType.Remainder);

        registerNumeric(OperationType.AdditionEquals);
        registerNumeric(OperationType.SubtractionEquals);
        registerNumeric(OperationType.MultiplicationEquals);
        registerNumeric(OperationType.DivisionEquals);
        registerNumeric(OperationType.RemainderEquals);

        registerUnaryNumeric(OperationType.Increment);
        registerUnaryNumeric(OperationType.Decrement);

        // String concatenation
        allow(OperationType.Addition,
                SymbolType.String,
                SymbolType.String,
                SymbolType.Character,
                SymbolType.Boolean,
                SymbolType.Short,
                SymbolType.Integer,
                SymbolType.Long,
                SymbolType.Float,
                SymbolType.Double,
                SymbolType.Decimal);

        // =========================================
        // Bitwise
        // =========================================

        registerIntegral(OperationType.BitAnd);
        registerIntegral(OperationType.BitOr);
        registerIntegral(OperationType.BitXor);
        registerIntegral(OperationType.BitShiftLeft);
        registerIntegral(OperationType.BitShiftRight);

        // Unary bit not
        registerUnaryIntegral(OperationType.BitNot);

        // =========================================
        // Logical
        // =========================================

        allow(OperationType.And,
                SymbolType.Boolean,
                SymbolType.Boolean);

        allow(OperationType.Or,
                SymbolType.Boolean,
                SymbolType.Boolean);

        allow(OperationType.Not,
                SymbolType.Boolean,
                SymbolType.Boolean);

        // =========================================
        // Comparisons
        // =========================================

        registerNumeric(OperationType.GreaterThan);
        registerNumeric(OperationType.LessThan);
        registerNumeric(OperationType.GreaterEqual);
        registerNumeric(OperationType.LessEqual);

        // =========================================
        // Equality
        // =========================================

        registerEquality(OperationType.Equality);
        registerEquality(OperationType.Inequality);
        registerEquality(OperationType.Equals);
    }

    private TypeCompatibility() {
    }

    // ============================================================
    // Registration Helpers
    // ============================================================

    /**
     * Registers C#-style numeric compatibility:
     *
     * short -> short,int,long,float,double,decimal
     * int   -> int,long,float,double,decimal
     * etc.
     */
    private static void registerNumeric(OperationType op) {

        SymbolType[] numeric = {
                SymbolType.Short,
                SymbolType.Integer,
                SymbolType.Long,
                SymbolType.Float,
                SymbolType.Double,
                SymbolType.Decimal
        };

        for (SymbolType left : numeric) {

            int leftRank = NUMERIC_RANK.get(left);

            for (SymbolType right : numeric) {

                int rightRank = NUMERIC_RANK.get(right);

                // Allow widening conversions only
                if (rightRank >= leftRank) {
                    allow(op, left, right);
                }
            }
        }
    }

    private static void registerUnaryNumeric(OperationType op) {

        SymbolType[] numeric = {
                SymbolType.Short,
                SymbolType.Integer,
                SymbolType.Long,
                SymbolType.Float,
                SymbolType.Double,
                SymbolType.Decimal
        };

        for (SymbolType type : numeric) {
            allow(op, type, type);
        }
    }

    private static void registerIntegral(OperationType op) {

        SymbolType[] integral = {
                SymbolType.Short,
                SymbolType.Integer,
                SymbolType.Long
        };

        for (SymbolType left : integral) {

            int leftRank = NUMERIC_RANK.get(left);

            for (SymbolType right : integral) {

                int rightRank = NUMERIC_RANK.get(right);

                if (rightRank >= leftRank) {
                    allow(op, left, right);
                }
            }
        }
    }

    private static void registerUnaryIntegral(OperationType op) {

        SymbolType[] integral = {
                SymbolType.Short,
                SymbolType.Integer,
                SymbolType.Long
        };

        for (SymbolType type : integral) {
            allow(op, type, type);
        }
    }

    private static void registerEquality(OperationType op) {

        for (SymbolType type : SymbolType.values()) {
            allow(op, type, type);
        }

        // Numeric cross comparisons
        registerNumeric(op);
    }

    private static void allow(OperationType operation,
                              SymbolType left,
                              SymbolType... compatible) {

        COMPATIBILITY
                .computeIfAbsent(operation,
                        k -> new EnumMap<>(SymbolType.class))
                .computeIfAbsent(left,
                        k -> EnumSet.noneOf(SymbolType.class))
                .addAll(Set.of(compatible));
    }

    // ============================================================
    // Public API
    // ============================================================

    public static boolean isCompatible(OperationType operation,
                                       SymbolType left,
                                       SymbolType right) {

        Map<SymbolType, Set<SymbolType>> operationMap =
                COMPATIBILITY.get(operation);

        if (operationMap == null) {
            return false;
        }

        Set<SymbolType> compatible =
                operationMap.get(left);

        if (compatible == null) {
            return false;
        }

        return compatible.contains(right);
    }
}
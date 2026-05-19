package compiler.gals;

public class SemanticTable {

    public enum Result { OK, WARNING, ERROR }

    // Índices: 0=int, 1=float, 2=char, 3=string, 4=bool, 5=null
    // Operações: 0=aritmética, 1=relacional, 2=lógica
    private static final SymbolType ERR = null;

    // ExpTable[tipo1][tipo2][op] → tipo resultado ou null=erro
    private static final SymbolType[][][] expTable = new SymbolType[6][6][3];

    // AtribTable[esq][dir] → OK/WARNING/ERROR
    private static final Result[][] atribTable = new Result[6][6];

    static {
        // Aritmética (op=0)
        expTable[0][0][0] = SymbolType.Integer;   // int + int = int
        expTable[0][1][0] = SymbolType.Float;     // int + float = float
        expTable[1][0][0] = SymbolType.Float;     // float + int = float
        expTable[1][1][0] = SymbolType.Float;     // float + float = float
        // tudo mais = null (erro)

        // Relacional (op=1) → resultado sempre bool
        expTable[0][0][1] = SymbolType.Boolean;
        expTable[0][1][1] = SymbolType.Boolean;
        expTable[1][0][1] = SymbolType.Boolean;
        expTable[1][1][1] = SymbolType.Boolean;

        // Lógica (op=2) → bool op bool = bool
        expTable[4][4][2] = SymbolType.Boolean;

        // AtribTable
        for (Result[] row : atribTable) java.util.Arrays.fill(row, Result.ERROR);
        atribTable[0][0] = Result.OK;     // int ← int
        atribTable[0][1] = Result.WARNING;// int ← float (aviso)
        atribTable[1][0] = Result.WARNING;// float ← int
        atribTable[1][1] = Result.OK;     // float ← float
        atribTable[2][2] = Result.OK;     // char ← char
        atribTable[3][3] = Result.OK;     // string ← string
        atribTable[4][4] = Result.OK;     // bool ← bool
    }

    public static int indexOf(SymbolType t) {
        return switch (t) {
            case Integer, Short, Long -> 0;
            case Float, Double, Decimal -> 1;
            case Character -> 2;
            case String -> 3;
            case Boolean -> 4;
            case Null -> 5;
        };
    }

    public static SymbolType resultType(SymbolType t1, SymbolType t2, int opCategory) {
        if (t1 == null || t2 == null) return null;
        return expTable[indexOf(t1)][indexOf(t2)][opCategory];
    }

    public static Result atribType(SymbolType left, SymbolType right) {
        if (left == null || right == null) return Result.ERROR;
        return atribTable[indexOf(left)][indexOf(right)];
    }
}
package act10;

public class ManejadorTipos {
    public static final String ERROR = "error_semantico";
    public static final String NUMBER = "number";
    public static final String BOOLEAN = "boolean";
    public static final String STRING = "string";

    private static int totalErrores = 0;

    public static void reportarError(int linea, int col, String causa) {
        totalErrores++;
        System.err.println("Error semántico en línea " + linea + ", columna " + col + ": " + causa);
    }

    public static int getTotalErrores() { return totalErrores; }
    public static void reiniciarErrores() { totalErrores = 0; }

    public static String validarSuma(String t1, String t2, int linea, int col) {
        if (t1.equals(ERROR) || t2.equals(ERROR)) return ERROR;
        if (t1.equals(STRING) || t2.equals(STRING)) return STRING;
        if (t1.equals(NUMBER) && t2.equals(NUMBER)) return NUMBER;

        reportarError(linea, col, "El operador '+' no es aplicable a los operandos de tipo '" + t1 + "' y '" + t2 + "'.");
        return ERROR;
    }

    public static String validarAritmetica(String t1, String t2, String op, int linea, int col) {
        if (t1.equals(ERROR) || t2.equals(ERROR)) return ERROR;
        if (t1.equals(NUMBER) && t2.equals(NUMBER)) return NUMBER;

        reportarError(linea, col, "El operador aritmético '" + op + 
            "' no es aplicable a los operandos de tipo '" + t1 + "' y '" + t2 + "'.");
        return ERROR;
    }

    public static String validarRelacional(String t1, String t2, String op, int linea, int col) {
        if (t1.equals(ERROR) || t2.equals(ERROR)) return ERROR;
        if (t1.equals(NUMBER) && t2.equals(NUMBER)) return BOOLEAN;

        reportarError(linea, col, "El operador relacional '" + op + 
            "' requiere operandos de tipo 'number', se recibió '" + t1 + "' y '" + t2 + "'.");
        return ERROR;
    }

    public static String validarIgualdad(String t1, String t2, String op, int linea, int col) {
        if (t1.equals(ERROR) || t2.equals(ERROR)) return ERROR;
        return BOOLEAN;
    }

    public static String validarLogica(String t1, String t2, String op, int linea, int col) {
        if (t1.equals(ERROR) || t2.equals(ERROR)) return ERROR;
        if (t1.equals(BOOLEAN) && t2.equals(BOOLEAN)) return BOOLEAN;

        reportarError(linea, col, "El operador lógico '" + op + 
            "' solo opera sobre operandos de tipo 'boolean', se recibió '" + t1 + "' y '" + t2 + "'.");
        return ERROR;
    }

    public static String validarNot(String t, int linea, int col) {
        if (t.equals(ERROR)) return ERROR;
        if (t.equals(BOOLEAN)) return BOOLEAN;

        reportarError(linea, col, "El operador de negación '!' requiere un operando de tipo 'boolean', se encontró '" + t + "'.");
        return ERROR;
    }

    public static void validarCondicion(String tipoCond, int linea, int col, String estructura) {
        if (tipoCond.equals(ERROR)) return;
        if (!tipoCond.equals(BOOLEAN)) {
            reportarError(linea, col, "La expresión dentro de la condición '" + estructura + 
                "' debe ser de tipo 'boolean', se encontró '" + tipoCond + "'.");
        }
    }
}
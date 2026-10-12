package act9;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TablaSimbolos {
    private List<Simbolo> simbolos;
    private Stack<String> pilaAmbitos;
    private int erroresDetectados;

    public TablaSimbolos() {
        this.simbolos = new ArrayList<>();
        this.pilaAmbitos = new Stack<>();
        this.pilaAmbitos.push("GLOBAL"); // Ámbito inicial
        this.erroresDetectados = 0;
    }

    public void entrarAmbito(String nombreAmbito) {
        pilaAmbitos.push(nombreAmbito);
    }

    public void salirAmbito() {
        if (pilaAmbitos.size() > 1) {
            pilaAmbitos.pop();
        }
    }

    public String getAmbitoActual() {
        return pilaAmbitos.peek();
    }

    public boolean esAmbitoGlobal() {
        return pilaAmbitos.peek().equals("GLOBAL");
    }

    // --- Validaciones de Nomenclatura (Casing) ---
    public static boolean esPascalCase(String s) {
        if (s == null || s.isEmpty()) return false;
        return Character.isUpperCase(s.charAt(0)) && !s.contains("_");
    }

    public static boolean esCamelCase(String s) {
        if (s == null || s.isEmpty()) return false;
        return Character.isLowerCase(s.charAt(0)) && !s.contains("_");
    }

    public static boolean esUpperSnakeCase(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.matches("^[A-Z0-9_]+$");
    }

    // --- Inserción con Validaciones ---
    public boolean insertar(String id, String tipoSimbolo, String tipoDato, Object valor, 
                            int linea, int columna, String token) {
        String ambito = getAmbitoActual();

        // 1. Validar Convención de Nomenclatura
        if (tipoSimbolo.equals("CLASE")) {
            if (!esPascalCase(id)) {
                System.err.println("Error sintáctico/semántico en línea " + linea + ", columna " + columna + 
                                   ": El identificador de clase '" + id + "' debe utilizar la convención PascalCase.");
                erroresDetectados++;
            }
        } else if (tipoSimbolo.equals("CONSTANTE")) {
            if (!esUpperSnakeCase(id) && !esCamelCase(id)) {
                System.err.println("Error sintáctico/semántico en línea " + linea + ", columna " + columna + 
                                   ": La constante '" + id + "' debe usar UPPER_SNAKE_CASE o camelCase.");
                erroresDetectados++;
            }
        } else { // METODO, VARIABLE, ATRIBUTO
            if (!esCamelCase(id)) {
                System.err.println("Error sintáctico/semántico en línea " + linea + ", columna " + columna + 
                                   ": El identificador '" + id + "' debe utilizar la convención camelCase.");
                erroresDetectados++;
            }
        }

        // 2. Validar Duplicados en el mismo ámbito
        for (Simbolo s : simbolos) {
            if (s.getId().equals(id) && s.getAmbito().equals(ambito)) {
                System.err.println("Error semántico en línea " + linea + ", columna " + columna + 
                                   ": El identificador '" + id + "' ya ha sido declarado en el ámbito '" + ambito + "'.");
                erroresDetectados++;
                return false;
            }
        }

        Simbolo nuevo = new Simbolo(id, tipoSimbolo, tipoDato, valor, ambito, linea, columna, token);
        simbolos.add(nuevo);
        return true;
    }

    // --- Verificar existencia de variable en el ámbito actual o superior ---
    public boolean existe(String id) {
        for (Simbolo s : simbolos) {
            if (s.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public int getErroresDetectados() {
        return erroresDetectados;
    }

    // --- Impresión Tabular ---
    public void imprimirTabla() {
        System.out.println("\n========================================================================================================================");
        System.out.println("                                                  TABLA DE SÍMBOLOS                                                     ");
        System.out.println("========================================================================================================================");
        System.out.println(String.format("%-18s %-13s %-11s %-16s %-17s %-7s %-9s %-16s", 
                "ID", "Categoría", "Tipo Dato", "Valor", "Ámbito", "Línea", "Columna", "Token Originador"));
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        for (Simbolo s : simbolos) {
            System.out.println(s);
        }
        System.out.println("========================================================================================================================");
        System.out.println("Total de símbolos registrados: " + simbolos.size() + "\n");
    }
}
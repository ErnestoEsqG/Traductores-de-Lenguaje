/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act12;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Tabla de símbolos con pila de ámbitos (global y bloques anidados { }).
 * Conserva un historial para poder mostrar también los símbolos de los
 * ámbitos que ya se cerraron.
 */
public class TablaSimbolos {
    private final Stack<Map<String, Simbolo>> pilaAmbitos;
    private final Stack<String> nombresAmbitos;
    private final List<Simbolo> historialSimbolos;
    private int contadorBloques;

    public TablaSimbolos() {
        pilaAmbitos = new Stack<>();
        nombresAmbitos = new Stack<>();
        historialSimbolos = new ArrayList<>();
        contadorBloques = 0;
        entrarAmbito("global");
    }

    public void entrarAmbito(String nombreAmbito) {
        pilaAmbitos.push(new LinkedHashMap<String, Simbolo>());
        nombresAmbitos.push(nombreAmbito);
    }

    /** Abre un ámbito de bloque con nombre único (bloque_1, bloque_2, ...). */
    public String entrarBloque() {
        contadorBloques++;
        entrarAmbito("bloque_" + contadorBloques);
        return obtenerAmbitoActual();
    }

    public void salirAmbito() {
        if (pilaAmbitos.size() > 1) {
            pilaAmbitos.pop();
            nombresAmbitos.pop();
        }
    }

    public String obtenerAmbitoActual() {
        return nombresAmbitos.peek();
    }

    public String obtenerAmbitoGlobal() {
        return nombresAmbitos.firstElement();
    }

    /** Inserta en el ámbito actual (let / const). Devuelve false si ya existía ahí. */
    public boolean insertar(Simbolo simbolo) {
        return insertarEn(pilaAmbitos.peek(), simbolo);
    }

    /** Inserta en el ámbito global ('var' no tiene ámbito de bloque en JavaScript). */
    public boolean insertarGlobal(Simbolo simbolo) {
        return insertarEn(pilaAmbitos.firstElement(), simbolo);
    }

    private boolean insertarEn(Map<String, Simbolo> ambito, Simbolo simbolo) {
        if (ambito.containsKey(simbolo.getNombre())) {
            return false;
        }
        ambito.put(simbolo.getNombre(), simbolo);
        historialSimbolos.add(simbolo);
        return true;
    }

    /** Busca desde el ámbito actual hacia el global. */
    public Simbolo buscar(String nombre) {
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            Simbolo simbolo = pilaAmbitos.get(i).get(nombre);
            if (simbolo != null) {
                return simbolo;
            }
        }
        return null;
    }

    public Simbolo buscarLocal(String nombre) {
        return pilaAmbitos.peek().get(nombre);
    }

    public Simbolo buscarGlobal(String nombre) {
        return pilaAmbitos.firstElement().get(nombre);
    }

    public List<Simbolo> getHistorialSimbolos() {
        return historialSimbolos;
    }

    public void imprimir() {
        String formato = "  %-12s %-7s %-10s %-10s %-6s %s%n";
        System.out.printf(formato, "NOMBRE", "CLASE", "TIPO", "AMBITO", "LINEA", "VALOR CONSTANTE");
        System.out.printf(formato, "------------", "-------", "----------", "----------", "------", "---------------");
        if (historialSimbolos.isEmpty()) {
            System.out.println("  (sin símbolos declarados)");
        }
        for (Simbolo s : historialSimbolos) {
            System.out.printf(formato, s.getNombre(), s.getClase(), s.getTipo(), s.getAmbito(),
                    s.getLinea(), s.getValorConstante() == null ? "-" : s.getValorConstante());
        }
    }
}

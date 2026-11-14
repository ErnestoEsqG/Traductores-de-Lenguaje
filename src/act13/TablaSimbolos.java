/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act13;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Tabla de símbolos con pila de ámbitos: global, funciones/métodos y bloques
 * anidados. Conserva un historial para mostrar también los símbolos de los
 * ámbitos que ya se cerraron.
 */
public class TablaSimbolos {

    private static class Ambito {
        final String nombre;
        /** true para el ámbito global y el de funciones/métodos (donde viven las 'var'). */
        final boolean esFuncion;
        final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

        Ambito(String nombre, boolean esFuncion) {
            this.nombre = nombre;
            this.esFuncion = esFuncion;
        }
    }

    private final Stack<Ambito> pilaAmbitos = new Stack<>();
    private final List<Simbolo> historialSimbolos = new ArrayList<>();
    private int contadorBloques = 0;

    public TablaSimbolos() {
        entrarAmbito("global", true);
    }

    public void entrarAmbito(String nombreAmbito, boolean esFuncion) {
        pilaAmbitos.push(new Ambito(nombreAmbito, esFuncion));
    }

    /** Abre un ámbito de bloque con nombre único (bloque_1, bloque_2, ...). */
    public String entrarBloque() {
        contadorBloques++;
        entrarAmbito("bloque_" + contadorBloques, false);
        return obtenerAmbitoActual();
    }

    public void salirAmbito() {
        if (pilaAmbitos.size() > 1) {
            pilaAmbitos.pop();
        }
    }

    public String obtenerAmbitoActual() {
        return pilaAmbitos.peek().nombre;
    }

    /** Ámbito de función (o global) más cercano: ahí se declaran las 'var'. */
    private Ambito ambitoFuncion() {
        for (int i = pilaAmbitos.size() - 1; i > 0; i--) {
            if (pilaAmbitos.get(i).esFuncion) {
                return pilaAmbitos.get(i);
            }
        }
        return pilaAmbitos.firstElement();
    }

    public String obtenerAmbitoFuncion() {
        return ambitoFuncion().nombre;
    }

    /** Inserta en el ámbito actual. Devuelve false si ya existía ahí. */
    public boolean insertar(Simbolo simbolo) {
        return insertarEn(pilaAmbitos.peek(), simbolo);
    }

    /** Inserta en el ámbito de función más cercano ('var' no tiene ámbito de bloque). */
    public boolean insertarEnAmbitoFuncion(Simbolo simbolo) {
        return insertarEn(ambitoFuncion(), simbolo);
    }

    private boolean insertarEn(Ambito ambito, Simbolo simbolo) {
        if (ambito.simbolos.containsKey(simbolo.getNombre())) {
            return false;
        }
        ambito.simbolos.put(simbolo.getNombre(), simbolo);
        historialSimbolos.add(simbolo);
        return true;
    }

    /** Agrega al historial un símbolo que vive fuera de la pila (atributos y métodos de clase). */
    public void registrar(Simbolo simbolo) {
        historialSimbolos.add(simbolo);
    }

    /** Busca desde el ámbito actual hacia el global. */
    public Simbolo buscar(String nombre) {
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            Simbolo simbolo = pilaAmbitos.get(i).simbolos.get(nombre);
            if (simbolo != null) {
                return simbolo;
            }
        }
        return null;
    }

    public Simbolo buscarLocal(String nombre) {
        return pilaAmbitos.peek().simbolos.get(nombre);
    }

    public Simbolo buscarEnAmbitoFuncion(String nombre) {
        return ambitoFuncion().simbolos.get(nombre);
    }

    public List<Simbolo> getHistorialSimbolos() {
        return historialSimbolos;
    }

    public void imprimir() {
        String formato = "  %-20s %-10s %-10s %-26s %-6s %s%n";
        System.out.printf(formato, "NOMBRE", "CATEGORIA", "TIPO", "AMBITO", "LINEA", "DETALLE");
        System.out.printf(formato, "--------------------", "----------", "----------",
                "--------------------------", "------", "------------------------------");
        if (historialSimbolos.isEmpty()) {
            System.out.println("  (sin símbolos declarados)");
        }
        for (Simbolo s : historialSimbolos) {
            System.out.printf(formato, s.getNombre(), s.getClase(), s.getTipo(), s.getAmbito(),
                    s.getLinea(), s.detalle());
        }
    }
}

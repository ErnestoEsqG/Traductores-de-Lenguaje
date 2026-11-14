/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act12;

import java.util.ArrayList;
import java.util.List;

/**
 * Generador de código intermedio de tres direcciones (TAC).
 * Administra las etiquetas temporales (t1, t2, ...) y guarda las instrucciones
 * emitidas junto con un comentario explicativo.
 */
public class GeneradorCodigoIntermedio {

    /** Una línea del listado: instrucción numerada o comentario suelto. */
    private static class Linea {
        final boolean esInstruccion;
        final String texto;
        final String comentario;

        Linea(boolean esInstruccion, String texto, String comentario) {
            this.esInstruccion = esInstruccion;
            this.texto = texto;
            this.comentario = comentario;
        }
    }

    private final List<Linea> lineas = new ArrayList<>();
    private final List<String> codigoFuente;
    private int contadorTemporales = 0;
    private int totalInstrucciones = 0;
    /** Posición del listado donde comienza el código de la sentencia en curso. */
    private int inicioSentencia = 0;

    public GeneradorCodigoIntermedio(List<String> codigoFuente) {
        this.codigoFuente = codigoFuente;
    }

    // ------------------------------------------------------------------
    // Temporales y emisión básica
    // ------------------------------------------------------------------

    public String nuevoTemporal() {
        contadorTemporales++;
        return "t" + contadorTemporales;
    }

    public void emitir(String instruccion, String comentario) {
        lineas.add(new Linea(true, instruccion, comentario));
        totalInstrucciones++;
    }

    /** Comentario que no cuenta como instrucción (ámbitos, avisos). */
    public void emitirComentario(String texto) {
        lineas.add(new Linea(false, "# " + texto, null));
    }

    /** Deja constancia de un error de compilación dentro del listado. */
    public void emitirError(String mensaje) {
        lineas.add(new Linea(false, "# !! ERROR: " + mensaje, null));
    }

    // ------------------------------------------------------------------
    // Estructura del listado
    // ------------------------------------------------------------------

    /** Cierra la sentencia actual anteponiéndole su línea de código fuente. */
    public void cerrarSentencia(int lineaFuente) {
        String fuente = "";
        if (lineaFuente >= 1 && lineaFuente <= codigoFuente.size()) {
            fuente = codigoFuente.get(lineaFuente - 1).trim();
        }
        lineas.add(inicioSentencia, new Linea(false, "# --- Línea " + lineaFuente + ": " + fuente, null));
        inicioSentencia = lineas.size();
    }

    public void marcarAmbito(String texto) {
        emitirComentario(texto);
        inicioSentencia = lineas.size();
    }

    // ------------------------------------------------------------------
    // Expresiones
    // ------------------------------------------------------------------

    /** Si el nodo es una operación pendiente (ej. "t6 && t7"), la guarda en un temporal. */
    public NodoExpresion materializar(NodoExpresion nodo) {
        if (!nodo.esPendiente()) {
            return nodo;
        }
        String t = nuevoTemporal();
        emitir(t + " = " + nodo.getLugar(), t + " es una etiqueta temporal para la operación " + nodo.getLugar());
        return NodoExpresion.temporal(nodo.getTipo(), t);
    }

    /** t = a op b */
    public NodoExpresion emitirBinaria(String op, NodoExpresion a, NodoExpresion b,
                                       TipoDato tipoResultado, boolean bloqueada) {
        String t = nuevoTemporal();
        String comentario = bloqueada
                ? "Este paso queda bloqueado por la validación anterior"
                : t + " es una etiqueta temporal para " + describir(op, a.getLugar(), b.getLugar());
        emitir(t + " = " + a.getLugar() + " " + op + " " + b.getLugar(), comentario);
        return NodoExpresion.temporal(tipoResultado, t);
    }

    /** Operación lógica cuyo resultado aún no se guarda: permite emitir "flag = t6 && t7". */
    public NodoExpresion operacionPendiente(String op, NodoExpresion a, NodoExpresion b, TipoDato tipo) {
        return NodoExpresion.pendiente(tipo, a.getLugar() + " " + op + " " + b.getLugar());
    }

    /** t = op a */
    public NodoExpresion emitirUnaria(String op, NodoExpresion a, TipoDato tipoResultado, boolean bloqueada) {
        String t = nuevoTemporal();
        String descripcion = op.equals("-") ? "el negativo de " : "la negación lógica (NOT) de ";
        String comentario = bloqueada
                ? "Este paso queda bloqueado por la validación anterior"
                : t + " es una etiqueta temporal para " + descripcion + a.getLugar();
        emitir(t + " = " + op + a.getLugar(), comentario);
        return NodoExpresion.temporal(tipoResultado, t);
    }

    // ------------------------------------------------------------------
    // Asignaciones
    // ------------------------------------------------------------------

    public void emitirAsignacion(String destino, NodoExpresion valor) {
        if (valor.esConstante()) {
            String t = nuevoTemporal();
            emitir(t + " = " + valor.getLugar(),
                    t + " es una etiqueta temporal que representa la constante " + valor.getLugar());
            emitir(destino + " = " + t, "Asigna el valor de " + t + " a " + destino);
        } else if (valor.esPendiente()) {
            emitir(destino + " = " + valor.getLugar(),
                    destino + " toma el valor de la operación " + valor.getLugar());
        } else {
            emitir(destino + " = " + valor.getLugar(),
                    "Asigna el valor de " + valor.getLugar() + " a " + destino);
        }
    }

    // ------------------------------------------------------------------
    // Validaciones
    // ------------------------------------------------------------------

    public void emitirValidacionDivision(NodoExpresion divisor) {
        emitir("if " + divisor.getLugar() + " == 0 then error \"Division by zero\"",
                "Valida que no se realice división por cero");
    }

    /** Valida que ambos operandos sean del tipo esperado. */
    public void emitirValidacionTipos(NodoExpresion a, NodoExpresion b, TipoDato esperado) {
        emitir("if type(" + a.getLugar() + ") != " + esperado + " or type(" + b.getLugar() + ") != "
                + esperado + " then error \"Type mismatch\"", "Valida tipos");
    }

    /** Valida que un único operando sea del tipo esperado. */
    public void emitirValidacionTipo(NodoExpresion a, TipoDato esperado) {
        emitir("if type(" + a.getLugar() + ") != " + esperado + " then error \"Type mismatch\"", "Valida tipos");
    }

    /** Valida que dos lugares tengan el mismo tipo (comparaciones de igualdad y asignaciones). */
    public void emitirValidacionMismoTipo(String a, String b) {
        emitir("if type(" + a + ") != type(" + b + ") then error \"Type mismatch\"", "Valida tipos");
    }

    // ------------------------------------------------------------------
    // Salida
    // ------------------------------------------------------------------

    public int getTotalTemporales() { return contadorTemporales; }
    public int getTotalInstrucciones() { return totalInstrucciones; }

    public void imprimir() {
        int ancho = 0;
        for (Linea l : lineas) {
            if (l.esInstruccion && l.texto.length() <= 44) {
                ancho = Math.max(ancho, l.texto.length());
            }
        }
        int numero = 0;
        for (Linea l : lineas) {
            if (!l.esInstruccion) {
                if (l.texto.startsWith("# ---")) {
                    System.out.println();
                }
                System.out.println("       " + l.texto);
            } else {
                numero++;
                System.out.printf("  %3d  %-" + Math.max(ancho, 1) + "s  # %s%n", numero, l.texto, l.comentario);
            }
        }
        if (lineas.isEmpty()) {
            System.out.println("  (no se generó código intermedio)");
        }
    }

    private String describir(String op, String a, String b) {
        // En español la conjunción "y" cambia a "e" antes del sonido "i" (x e y)
        String y = (b.startsWith("y") || b.startsWith("i")) ? " e " : " y ";
        switch (op) {
            case "+":  return "la suma de " + a + y + b;
            case "-":  return "la resta de " + a + y + b;
            case "*":  return "la multiplicación de " + a + " por " + b;
            case "/":  return "la división de " + a + " entre " + b;
            case "%":  return "el módulo de " + a + " entre " + b;
            case "&&": return "la operación lógica AND entre " + a + y + b;
            case "||": return "la operación lógica OR entre " + a + y + b;
            default:   return "la comparación " + a + " " + op + " " + b;
        }
    }
}

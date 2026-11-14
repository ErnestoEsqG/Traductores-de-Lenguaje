/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act13;

import java.util.ArrayList;
import java.util.List;

/**
 * Generador de código intermedio de tres direcciones (TAC).
 * Administra las etiquetas temporales (t1, t2, ...) y las etiquetas de salto
 * (L1, L2, ...), y guarda las instrucciones emitidas con su comentario.
 * Permite mover e insertar bloques de instrucciones, lo que se usa para
 * acomodar el código de if/else, switch y for en el orden requerido.
 */
public class GeneradorCodigoIntermedio {

    /**
     * true: "x = 5" se emite como "t1 = 5; x = t1" (regla de la Actividad 12).
     * false: se emite "x = 5" directamente, como en los ejemplos de la Actividad 13.
     */
    public static final boolean CONSTANTES_EN_TEMPORAL = false;

    /** Una línea del listado: instrucción, etiqueta de salto o comentario. */
    public static class Linea {
        private enum Tipo { INSTRUCCION, ETIQUETA, COMENTARIO }

        private final Tipo tipo;
        private String texto;
        private String comentario;

        private Linea(Tipo tipo, String texto, String comentario) {
            this.tipo = tipo;
            this.texto = texto;
            this.comentario = contraer(comentario);
        }
    }

    /** Aplica las contracciones del español: "de el" -> "del", "a el" -> "al". */
    private static String contraer(String texto) {
        return texto == null ? null : texto.replace(" de el ", " del ").replace(" a el ", " al ");
    }

    private final List<Linea> lineas = new ArrayList<>();
    private final List<String> codigoFuente;
    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;
    private int ultimaLineaMarcada = -1;

    public GeneradorCodigoIntermedio(List<String> codigoFuente) {
        this.codigoFuente = codigoFuente;
    }

    // ------------------------------------------------------------------
    // Temporales, etiquetas y emisión básica
    // ------------------------------------------------------------------

    public String nuevoTemporal() {
        contadorTemporales++;
        return "t" + contadorTemporales;
    }

    public String nuevaEtiqueta() {
        contadorEtiquetas++;
        return "L" + contadorEtiquetas;
    }

    /** Crea una instrucción sin agregarla al listado (para insertarla después). */
    public Linea crearInstruccion(String instruccion, String comentario) {
        return new Linea(Linea.Tipo.INSTRUCCION, instruccion, comentario);
    }

    public Linea emitir(String instruccion, String comentario) {
        Linea linea = crearInstruccion(instruccion, comentario);
        lineas.add(linea);
        return linea;
    }

    public void emitirEtiqueta(String etiqueta, String comentario) {
        lineas.add(new Linea(Linea.Tipo.ETIQUETA, etiqueta + ":", comentario));
    }

    public void emitirComentario(String texto) {
        lineas.add(new Linea(Linea.Tipo.COMENTARIO, contraer("# " + texto), null));
    }

    /** Deja constancia de un error de compilación dentro del listado. */
    public void emitirError(String mensaje) {
        lineas.add(new Linea(Linea.Tipo.COMENTARIO, "# !! ERROR: " + mensaje, null));
    }

    /** Antepone la línea de código fuente la primera vez que se emite código para ella. */
    public void marcarLinea(int lineaFuente) {
        if (lineaFuente == ultimaLineaMarcada || lineaFuente < 1 || lineaFuente > codigoFuente.size()) {
            return;
        }
        ultimaLineaMarcada = lineaFuente;
        lineas.add(new Linea(Linea.Tipo.COMENTARIO,
                "# --- Línea " + lineaFuente + ": " + codigoFuente.get(lineaFuente - 1).trim(), null));
    }

    /** Completa un salto cuyo destino no se conocía al emitirlo (break / continue). */
    public void parchar(Linea linea, String instruccion, String comentario) {
        linea.texto = instruccion;
        linea.comentario = contraer(comentario);
    }

    // ------------------------------------------------------------------
    // Reacomodo de bloques
    // ------------------------------------------------------------------

    public int posicion() {
        return lineas.size();
    }

    /** Retira del listado las líneas [desde, hasta) y las devuelve. */
    public List<Linea> extraer(int desde, int hasta) {
        List<Linea> bloque = new ArrayList<>(lineas.subList(desde, hasta));
        lineas.subList(desde, hasta).clear();
        return bloque;
    }

    public void insertar(int posicion, List<Linea> bloque) {
        lineas.addAll(posicion, bloque);
    }

    public void agregar(List<Linea> bloque) {
        lineas.addAll(bloque);
    }

    private Linea ultimaLinea() {
        return lineas.isEmpty() ? null : lineas.get(lineas.size() - 1);
    }

    /** true si la última instrucción emitida es un return (evita duplicarlo al cerrar una función). */
    public boolean ultimaEsReturn() {
        Linea ultima = ultimaLinea();
        return ultima != null && ultima.tipo == Linea.Tipo.INSTRUCCION
                && (ultima.texto.equals("return") || ultima.texto.startsWith("return "));
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
        return NodoExpresion.temporal(nodo.getTipo(), t, null);
    }

    /** t = a op b */
    public NodoExpresion emitirBinaria(String op, NodoExpresion a, NodoExpresion b,
                                       TipoDato tipoResultado, boolean bloqueada) {
        String t = nuevoTemporal();
        String comentario = bloqueada
                ? "Este paso queda bloqueado por la validación anterior"
                : t + " es una etiqueta temporal para " + describir(op, a.getLugar(), b.getLugar());
        emitir(t + " = " + a.getLugar() + " " + op + " " + b.getLugar(), comentario);
        return NodoExpresion.temporal(tipoResultado, t, null);
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
        return NodoExpresion.temporal(tipoResultado, t, null);
    }

    /** Guarda en un temporal nuevo un valor especial: "return_value" o "create Clase". */
    public NodoExpresion emitirCaptura(String origen, TipoDato tipo, String clase, String comentario) {
        String t = nuevoTemporal();
        emitir(t + " = " + origen, t + " " + comentario);
        return NodoExpresion.temporal(tipo, t, clase);
    }

    // ------------------------------------------------------------------
    // Asignaciones
    // ------------------------------------------------------------------

    public void emitirAsignacion(String destino, NodoExpresion valor) {
        if (valor.esConstante()) {
            if (CONSTANTES_EN_TEMPORAL) {
                String t = nuevoTemporal();
                emitir(t + " = " + valor.getLugar(),
                        t + " es una etiqueta temporal que representa la constante " + valor.getLugar());
                emitir(destino + " = " + t, "Asigna el valor de " + t + " a " + destino);
            } else {
                emitir(destino + " = " + valor.getLugar(), "Asigna la constante " + valor.getLugar() + " a " + destino);
            }
            return;
        }
        if (valor.esPendiente()) {
            emitir(destino + " = " + valor.getLugar(), destino + " toma el valor de la operación " + valor.getLugar());
            return;
        }
        // "tN = return_value; x = tN"  se simplifica a  "x = return_value" (igual con "create Clase")
        String origen = retirarCaptura(valor);
        if (origen != null) {
            String comentario = origen.equals("return_value")
                    ? "Asigna el valor retornado por la llamada a " + destino
                    : "Representa la creación del objeto " + destino;
            emitir(destino + " = " + origen, comentario);
            return;
        }
        emitir(destino + " = " + valor.getLugar(), "Asigna el valor de " + valor.getLugar() + " a " + destino);
    }

    /**
     * Si la última instrucción es "tN = return_value" o "tN = create X" y tN es el
     * nodo recibido, la retira, libera el temporal y devuelve el origen; si no, null.
     */
    private String retirarCaptura(NodoExpresion nodo) {
        Linea ultima = ultimaLinea();
        if (!nodo.esTemporal() || ultima == null || ultima.tipo != Linea.Tipo.INSTRUCCION
                || !nodo.getLugar().equals("t" + contadorTemporales)) {
            return null;
        }
        String prefijo = nodo.getLugar() + " = ";
        if (!ultima.texto.startsWith(prefijo)) {
            return null;
        }
        String origen = ultima.texto.substring(prefijo.length());
        if (!origen.equals("return_value") && !origen.startsWith("create ")) {
            return null;
        }
        lineas.remove(lineas.size() - 1);
        contadorTemporales--;
        return origen;
    }

    /** Una llamada usada como sentencia no necesita guardar su valor de retorno. */
    public void descartarValorDeRetorno(NodoExpresion nodo) {
        retirarCaptura(nodo);
    }

    // ------------------------------------------------------------------
    // Validaciones
    // ------------------------------------------------------------------

    public void emitirValidacionDivision(NodoExpresion divisor) {
        emitir("if " + divisor.getLugar() + " == 0 then error \"Division by zero\"",
                "Valida que no se realice división por cero");
    }

    public void emitirValidacionTipos(NodoExpresion a, NodoExpresion b, TipoDato esperado) {
        emitir("if type(" + a.getLugar() + ") != " + esperado + " or type(" + b.getLugar() + ") != "
                + esperado + " then error \"Type mismatch\"", "Valida tipos");
    }

    public void emitirValidacionTipo(String lugar, TipoDato esperado) {
        emitir("if type(" + lugar + ") != " + esperado + " then error \"Type mismatch\"", "Valida tipos");
    }

    public void emitirValidacionMismoTipo(String a, String b) {
        emitir("if type(" + a + ") != type(" + b + ") then error \"Type mismatch\"", "Valida tipos");
    }

    // ------------------------------------------------------------------
    // Salida
    // ------------------------------------------------------------------

    public int getTotalTemporales() { return contadorTemporales; }
    public int getTotalEtiquetas() { return contadorEtiquetas; }

    public int getTotalInstrucciones() {
        int total = 0;
        for (Linea l : lineas) {
            if (l.tipo == Linea.Tipo.INSTRUCCION) {
                total++;
            }
        }
        return total;
    }

    public void imprimir() {
        final String sangria = "    ";
        int ancho = 1;
        for (Linea l : lineas) {
            int largo = l.texto.length() + (l.tipo == Linea.Tipo.INSTRUCCION ? sangria.length() : 0);
            if (l.tipo != Linea.Tipo.COMENTARIO && largo <= 48) {
                ancho = Math.max(ancho, largo);
            }
        }
        int numero = 0;
        for (Linea l : lineas) {
            if (l.tipo == Linea.Tipo.COMENTARIO) {
                if (l.texto.startsWith("# ---")) {
                    System.out.println();
                }
                System.out.println("       " + l.texto);
            } else {
                numero++;
                String texto = (l.tipo == Linea.Tipo.INSTRUCCION ? sangria : "") + l.texto;
                System.out.printf("  %3d  %-" + ancho + "s  # %s%n", numero, texto, l.comentario);
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

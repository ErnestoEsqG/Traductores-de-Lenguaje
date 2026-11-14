/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act13;

/**
 * Atributo sintetizado de una expresión. Transporta su tipo, el "lugar" donde
 * queda su valor en el código intermedio (literal, variable o temporal) y si
 * es una constante.
 */
public class NodoExpresion {
    private enum Forma { CONSTANTE, VARIABLE, TEMPORAL, PENDIENTE }

    private final Forma forma;
    private final TipoDato tipo;
    private final String lugar;
    /** Valor conocido en compilación (literal o 'const' propagada); null si se desconoce. */
    private final String valor;
    /** Para objetos: nombre de su clase; null en otro caso. */
    private final String clase;

    private NodoExpresion(Forma forma, TipoDato tipo, String lugar, String valor, String clase) {
        this.forma = forma;
        this.tipo = tipo;
        this.lugar = lugar;
        this.valor = valor;
        this.clase = clase;
    }

    /** Literal del código fuente: 5, "hola", true. */
    public static NodoExpresion constante(TipoDato tipo, String lexema) {
        return new NodoExpresion(Forma.CONSTANTE, tipo, lexema, lexema, null);
    }

    /** Uso de una variable o atributo; valorConocido solo se informa para 'const' con valor literal. */
    public static NodoExpresion variable(TipoDato tipo, String nombre, String valorConocido, String clase) {
        return new NodoExpresion(Forma.VARIABLE, tipo, nombre, valorConocido, clase);
    }

    /** Resultado guardado en una etiqueta temporal (t1, t2, ...). */
    public static NodoExpresion temporal(TipoDato tipo, String temporal, String clase) {
        return new NodoExpresion(Forma.TEMPORAL, tipo, temporal, null, clase);
    }

    /** Operación todavía no asignada a un temporal (ej. "t6 && t7"). */
    public static NodoExpresion pendiente(TipoDato tipo, String operacion) {
        return new NodoExpresion(Forma.PENDIENTE, tipo, operacion, null, null);
    }

    public TipoDato getTipo() { return tipo; }
    public String getLugar() { return lugar; }
    public String getValor() { return valor; }
    public String getClase() { return clase; }
    public boolean esConstante() { return forma == Forma.CONSTANTE; }
    public boolean esTemporal() { return forma == Forma.TEMPORAL; }
    public boolean esPendiente() { return forma == Forma.PENDIENTE; }

    /** true si en compilación se sabe que el valor numérico es cero. */
    public boolean esCeroConocido() {
        if (valor == null || tipo != TipoDato.NUMBER) {
            return false;
        }
        try {
            return Double.parseDouble(valor) == 0.0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    public String toString() {
        return lugar + " : " + tipo;
    }
}

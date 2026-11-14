/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act12;

/**
 * Atributo sintetizado de una expresión. Transporta su tipo, el "lugar" donde
 * queda su valor en el código intermedio (literal, variable o temporal) y si
 * es una constante.
 */
public class NodoExpresion {
    private final TipoDato tipo;
    private final String lugar;
    private final boolean constante;
    /** true si 'lugar' es una operación aún no guardada en un temporal (ej. "t6 && t7"). */
    private final boolean pendiente;
    /** Valor conocido en compilación (literal o 'const' propagada); null si se desconoce. */
    private final String valor;

    private NodoExpresion(TipoDato tipo, String lugar, boolean constante, boolean pendiente, String valor) {
        this.tipo = tipo;
        this.lugar = lugar;
        this.constante = constante;
        this.pendiente = pendiente;
        this.valor = valor;
    }

    /** Literal del código fuente: 5, "hola", true. */
    public static NodoExpresion constante(TipoDato tipo, String lexema) {
        return new NodoExpresion(tipo, lexema, true, false, lexema);
    }

    /** Uso de una variable; valorConocido solo se informa para 'const' con valor literal. */
    public static NodoExpresion variable(TipoDato tipo, String nombre, String valorConocido) {
        return new NodoExpresion(tipo, nombre, false, false, valorConocido);
    }

    /** Resultado guardado en una etiqueta temporal (t1, t2, ...). */
    public static NodoExpresion temporal(TipoDato tipo, String temporal) {
        return new NodoExpresion(tipo, temporal, false, false, null);
    }

    /** Operación todavía no asignada a un temporal. */
    public static NodoExpresion pendiente(TipoDato tipo, String operacion) {
        return new NodoExpresion(tipo, operacion, false, true, null);
    }

    public TipoDato getTipo() { return tipo; }
    public String getLugar() { return lugar; }
    public boolean esConstante() { return constante; }
    public boolean esPendiente() { return pendiente; }
    public String getValor() { return valor; }

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

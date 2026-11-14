/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act12;

/** Entrada de la tabla de símbolos: una variable o constante de JavaScript. */
public class Simbolo {
    /** Palabra reservada con la que se declaró el identificador. */
    public enum Clase {
        LET("let"), CONST("const"), VAR("var");

        private final String lexema;

        Clase(String lexema) {
            this.lexema = lexema;
        }

        @Override
        public String toString() {
            return lexema;
        }
    }

    private final String nombre;
    private final Clase clase;
    private final String ambito;
    private final int linea;
    private final int columna;
    private TipoDato tipo;
    /** Valor literal conocido en compilación (solo para 'const' inicializadas con un literal). */
    private String valorConstante;

    public Simbolo(String nombre, Clase clase, TipoDato tipo, String ambito, int linea, int columna) {
        this.nombre = nombre;
        this.clase = clase;
        this.tipo = tipo;
        this.ambito = ambito;
        this.linea = linea;
        this.columna = columna;
    }

    public String getNombre() { return nombre; }
    public Clase getClase() { return clase; }
    public String getAmbito() { return ambito; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    public TipoDato getTipo() { return tipo; }
    public void setTipo(TipoDato tipo) { this.tipo = tipo; }
    public String getValorConstante() { return valorConstante; }
    public void setValorConstante(String valorConstante) { this.valorConstante = valorConstante; }
    public boolean esConstante() { return clase == Clase.CONST; }
}

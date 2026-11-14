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

/** Entrada de la tabla de símbolos: variable, constante, parámetro, función, clase, atributo o método. */
public class Simbolo {
    /** Categoría del identificador. */
    public enum Clase {
        LET("let"), CONST("const"), VAR("var"), PARAMETRO("parámetro"),
        FUNCION("función"), CLASE("clase"), ATRIBUTO("atributo"), METODO("método");

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
    /** Tipo de la variable o, en funciones y métodos, tipo del valor de retorno. */
    private TipoDato tipo;
    /** Valor literal conocido en compilación (solo 'const' inicializadas con un literal). */
    private String valorConstante;
    /** Para objetos: nombre de la clase de la que son instancia. */
    private String nombreClase;
    /** Para funciones y métodos: etiqueta donde inicia su código. */
    private String etiqueta;
    /** Para funciones y métodos: nombres de sus parámetros. */
    private List<String> parametros = new ArrayList<>();
    /** Para funciones y métodos: true si contienen al menos un 'return valor;'. */
    private boolean retornaValor;
    /** Para clases: atributos y métodos. */
    private final Map<String, Simbolo> miembros = new LinkedHashMap<>();

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
    public String getNombreClase() { return nombreClase; }
    public void setNombreClase(String nombreClase) { this.nombreClase = nombreClase; }
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
    public List<String> getParametros() { return parametros; }
    public void setParametros(List<String> parametros) { this.parametros = parametros; }
    public boolean retornaValor() { return retornaValor; }
    public void setRetornaValor(boolean retornaValor) { this.retornaValor = retornaValor; }
    public Simbolo getMiembro(String nombreMiembro) { return miembros.get(nombreMiembro); }
    public void agregarMiembro(Simbolo miembro) { miembros.put(miembro.getNombre(), miembro); }

    public boolean esConstante() { return clase == Clase.CONST; }

    /** true si se le puede asignar o leer como variable. */
    public boolean esVariable() {
        return clase == Clase.LET || clase == Clase.CONST || clase == Clase.VAR || clase == Clase.PARAMETRO;
    }

    public boolean esRutina() {
        return clase == Clase.FUNCION || clase == Clase.METODO;
    }

    /** Información adicional para mostrar en la tabla de símbolos. */
    public String detalle() {
        if (esRutina()) {
            return "inicia en " + etiqueta + ", parámetros (" + String.join(", ", parametros) + ")";
        }
        if (clase == Clase.CLASE) {
            return "miembros: " + String.join(", ", miembros.keySet());
        }
        if (valorConstante != null) {
            return "valor constante " + valorConstante;
        }
        if (tipo == TipoDato.OBJECT && nombreClase != null) {
            return "instancia de " + nombreClase;
        }
        return "-";
    }
}

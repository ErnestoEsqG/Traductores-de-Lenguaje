/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act13;

/** Tipos de dato que el compilador infiere para el subconjunto de JavaScript. */
public enum TipoDato {
    NUMBER("number"),
    STRING("string"),
    BOOLEAN("boolean"),
    OBJECT("object"),
    /** Tipo desconocido en compilación (parámetros de funciones). */
    ANY("any"),
    UNDEFINED("undefined"),
    ERROR("error");

    private final String nombre;

    TipoDato(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

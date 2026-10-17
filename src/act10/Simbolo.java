package act10;

public class Simbolo {
    private String nombre;
    private String tipoActual; // "number", "boolean", "string", "indefinido"
    private boolean esConstante;
    private int linea;
    private int columna;

    public Simbolo(String nombre, String tipoActual, boolean esConstante, int linea, int columna) {
        this.nombre = nombre;
        this.tipoActual = tipoActual;
        this.esConstante = esConstante;
        this.linea = linea;
        this.columna = columna;
    }

    public String getNombre() { return nombre; }
    public String getTipoActual() { return tipoActual; }
    public void setTipoActual(String tipoActual) { this.tipoActual = tipoActual; }
    public boolean esConstante() { return esConstante; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
}
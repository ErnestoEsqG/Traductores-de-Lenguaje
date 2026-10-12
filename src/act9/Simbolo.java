package act9;

public class Simbolo {
    private String id;
    private String tipoSimbolo; // CLASE, METODO, VARIABLE, ATRIBUTO, CONSTANTE
    private String tipoDato;    // int, float, string, boolean, void, any
    private Object valor;       // Valor actual o null
    private String ambito;      // GLOBAL, NombreClase, NombreMetodo
    private int linea;          // Línea de origen
    private int columna;        // Columna de origen
    private String token;       // Token originador (ej. ID)

    public Simbolo(String id, String tipoSimbolo, String tipoDato, Object valor, 
                   String ambito, int linea, int columna, String token) {
        this.id = id;
        this.tipoSimbolo = tipoSimbolo;
        this.tipoDato = tipoDato;
        this.valor = valor;
        this.ambito = ambito;
        this.linea = linea;
        this.columna = columna;
        this.token = token;
    }

    public String getId() { return id; }
    public String getTipoSimbolo() { return tipoSimbolo; }
    public String getTipoDato() { return tipoDato; }
    public Object getValor() { return valor; }
    public String getAmbito() { return ambito; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    public String getToken() { return token; }

    @Override
    public String toString() {
        return String.format("%-18s %-13s %-11s %-16s %-17s %-7d %-9d %-16s",
                id, tipoSimbolo, tipoDato, 
                (valor == null ? "null" : valor.toString()), 
                ambito, linea, columna, token);
    }
}
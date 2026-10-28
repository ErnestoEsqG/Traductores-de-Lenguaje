package act11;

import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {
    public enum TipoAmbito { GLOBAL, CLASE, FUNCION, BLOQUE }

    private String nombreAmbito;
    private TipoAmbito tipoAmbito;
    private TablaSimbolos anterior;
    private Map<String, Simbolo> tabla;

    public TablaSimbolos(String nombreAmbito, TipoAmbito tipoAmbito, TablaSimbolos anterior) {
        this.nombreAmbito = nombreAmbito;
        this.tipoAmbito = tipoAmbito;
        this.anterior = anterior;
        this.tabla = new HashMap<>();
    }

    public boolean insertar(Simbolo s) {
        if (tabla.containsKey(s.getNombre())) {
            return false;
        }
        tabla.put(s.getNombre(), s);
        return true;
    }

    public Simbolo buscar(String nombre) {
        for (TablaSimbolos e = this; e != null; e = e.anterior) {
            Simbolo s = e.tabla.get(nombre);
            if (s != null) return s;
        }
        return null;
    }

    public Simbolo buscarLocal(String nombre) {
        return tabla.get(nombre);
    }

    public TablaSimbolos getAnterior() { return anterior; }
    public TipoAmbito getTipoAmbito() { return tipoAmbito; }
    public String getNombreAmbito() { return nombreAmbito; }
    public Map<String, Simbolo> getTabla() { return tabla; }
}
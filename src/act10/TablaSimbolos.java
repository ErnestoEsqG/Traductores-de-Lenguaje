package act10;

import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {
    private Map<String, Simbolo> tabla = new HashMap<>();

    public boolean declarar(String nombre, String tipoInicial, boolean esConstante, int linea, int col) {
        if (tabla.containsKey(nombre)) {
            ManejadorTipos.reportarError(linea, col, 
                "El identificador '" + nombre + "' ya ha sido declarado en este ámbito.");
            return false;
        }
        tabla.put(nombre, new Simbolo(nombre, tipoInicial, esConstante, linea, col));
        return true;
    }

    public void asignar(String nombre, String nuevoTipo, int linea, int col) {
    Simbolo s = tabla.get(nombre);
    if (s == null) {
        ManejadorTipos.reportarError(linea, col, 
            "El identificador '" + nombre + "' no ha sido declarado en este ámbito.");
        return;
    }
    if (s.esConstante()) {
        ManejadorTipos.reportarError(linea, col, 
            "No se puede reasignar un valor a la constante '" + nombre + "'.");
        return;
    }

    // SI LA EXPRESIÓN DIO ERROR, NO SOBRESCRIBAS EL TIPO ANTERIOR DE LA VARIABLE
    if (nuevoTipo.equals(ManejadorTipos.ERROR)) {
        return;
    }

    s.setTipoActual(nuevoTipo);
}

    public Simbolo buscar(String nombre) {
        return tabla.get(nombre);
    }
}
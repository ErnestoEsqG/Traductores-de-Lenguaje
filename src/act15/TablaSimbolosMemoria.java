/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act15;

import java.util.*;

public class TablaSimbolosMemoria {

    public static class EntradaSimbolo {
        public final String nombre;
        public final String tipo;      // VARIABLE, SALIDA, TEMPORAL, ETIQUETA
        public final String segmento;  // DATOS, SALIDA, CODIGO
        public final int direccion;

        public EntradaSimbolo(String nombre, String tipo, String segmento, int direccion) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.segmento = segmento;
            this.direccion = direccion;
        }

        public String getDireccionHex() {
            return String.format("0x%04X", direccion);
        }
    }

    private final Map<String, EntradaSimbolo> tabla = new LinkedHashMap<>();
    private int siguienteDirDatos = 0x2000;
    private int siguienteDirSalida = 0x3000;

    public int registrarVariable(String nombre, boolean esSalida) {
        if (tabla.containsKey(nombre)) {
            return tabla.get(nombre).direccion;
        }
        int dir;
        String tipo;
        String segmento;
        if (esSalida) {
            dir = siguienteDirSalida;
            siguienteDirSalida += 4;
            tipo = "SALIDA";
            segmento = "SALIDA";
        } else {
            dir = siguienteDirDatos;
            siguienteDirDatos += 4;
            tipo = nombre.matches("t\\d+") ? "TEMPORAL" : "VARIABLE";
            segmento = "DATOS";
        }
        tabla.put(nombre, new EntradaSimbolo(nombre, tipo, segmento, dir));
        return dir;
    }

    public void registrarEtiqueta(String nombre, int direccionCodigo) {
        tabla.put(nombre, new EntradaSimbolo(nombre, "ETIQUETA", "CODIGO", direccionCodigo));
    }

    public Integer obtenerDireccion(String nombre) {
        EntradaSimbolo e = tabla.get(nombre);
        return e != null ? e.direccion : null;
    }

    public String obtenerDireccionHex(String nombre) {
        EntradaSimbolo e = tabla.get(nombre);
        return e != null ? e.getDireccionHex() : nombre;
    }

    public Collection<EntradaSimbolo> getEntradas() {
        return Collections.unmodifiableCollection(tabla.values());
    }

    public void imprimirTabla() {
        System.out.println("   +--------------+------------+------------+-----------+");
        System.out.println("   | Símbolo      | Tipo       | Segmento   | Dirección |");
        System.out.println("   +--------------+------------+------------+-----------+");
        for (EntradaSimbolo e : tabla.values()) {
            System.out.printf("   | %-12s | %-10s | %-10s | %-9s |%n",
                    e.nombre, e.tipo, e.segmento, e.getDireccionHex());
        }
        System.out.println("   +--------------+------------+------------+-----------+");
    }
}
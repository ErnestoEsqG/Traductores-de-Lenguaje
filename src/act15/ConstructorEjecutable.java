/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act15;

import act15.GeneradorCodigoMaquina.InstruccionMaquina;
import act15.TablaSimbolosMemoria.EntradaSimbolo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConstructorEjecutable {

    private static final String DIRECTORIO_SALIDA = "salida_act15";

    /**
     * Genera el archivo ejecutable con cabecera, segmento de código y metadatos en disco.
     */
    public Path generarArchivoEjecutable(
            String nombreArchivo,
            List<InstruccionMaquina> codigoMaquina,
            TablaSimbolosMemoria tablaSimbolos) throws IOException {

        Path dir = Paths.get(DIRECTORIO_SALIDA);
        Files.createDirectories(dir);
        Path rutaArchivo = dir.resolve(nombreArchivo);

        int tamanoCodigoBytes = codigoMaquina.size() * 4;
        long cantDatos = tablaSimbolos.getEntradas().stream()
                .filter(e -> !e.segmento.equals("CODIGO")).count();
        int tamanoDatosBytes = (int) cantDatos * 4;

        StringBuilder sb = new StringBuilder();
        sb.append("# ==========================================================\n");
        sb.append("# ARCHIVO EJECUTABLE GENERADO - COMPILADOR ACT 15\n");
        sb.append("# ==========================================================\n");
        sb.append("[HEADER]\n");
        sb.append("MAGIC_NUMBER   = 0x7F434D50 (CMP15)\n");
        sb.append("ARCHITECTURE   = RISC-32-SIM\n");
        sb.append("ENTRY_POINT    = 0x1000\n");
        sb.append("CODE_SIZE      = ").append(tamanoCodigoBytes).append(" bytes\n");
        sb.append("DATA_SIZE      = ").append(tamanoDatosBytes).append(" bytes\n");
        sb.append("RUNTIME_LIBS   = libmath_rt.so, libio_rt.so\n\n");

        sb.append("[SYMBOL_TABLE]\n");
        for (EntradaSimbolo e : tablaSimbolos.getEntradas()) {
            sb.append(String.format("%-12s %-10s %s%n", e.nombre, e.segmento, e.getDireccionHex()));
        }
        sb.append("\n[CODE_SEGMENT]\n");
        for (InstruccionMaquina im : codigoMaquina) {
            sb.append(String.format("0x%04X: %s  ; %s%n", im.direccion, im.codigoHex, im.formatoSimbolico()));
        }

        Files.writeString(rutaArchivo, sb.toString());

        // Configuración del entorno: asignar permisos de ejecución al archivo generado
        File archivo = rutaArchivo.toFile();
        archivo.setExecutable(true, false);

        return rutaArchivo;
    }

    /**
     * Configura el entorno de ejecución (librerías, permisos, memoria) y ejecuta el código máquina.
     */
    public Map<String, Integer> configurarYEjecutar(
            Path archivoEjecutable,
            List<InstruccionMaquina> codigoMaquina,
            TablaSimbolosMemoria tablaSimbolos,
            Map<String, Integer> valoresIniciales) {

        File f = archivoEjecutable.toFile();
        System.out.println("   [Entorno] Archivo ejecutable : " + archivoEjecutable.toAbsolutePath());
        System.out.println("   [Entorno] Permiso ejecución  : " + (f.canExecute() ? "HABILITADO (+x)" : "ESTANDAR"));
        System.out.println("   [Entorno] Librerías cargadas : libmath_rt.so, libio_rt.so");
        System.out.println("   [Entorno] Punto de entrada   : 0x1000");

        // Cargar memoria de datos (dirección -> valor)
        Map<Integer, Integer> memoria = new HashMap<>();
        for (Map.Entry<String, Integer> entry : valoresIniciales.entrySet()) {
            Integer dir = tablaSimbolos.obtenerDireccion(entry.getKey());
            if (dir != null) {
                memoria.put(dir, entry.getValue());
            }
        }

        // Mapa de dirección de instrucción -> índice en la lista
        Map<Integer, Integer> mapaPC = new HashMap<>();
        for (int i = 0; i < codigoMaquina.size(); i++) {
            mapaPC.put(codigoMaquina.get(i).direccion, i);
        }

        int r1 = 0;
        int flagCmp = 0;
        int idx = 0;
        int ciclos = 0;

        while (idx >= 0 && idx < codigoMaquina.size() && ciclos < 500) {
            ciclos++;
            InstruccionMaquina im = codigoMaquina.get(idx);

            switch (im.opcode) {
                case "LOAD" -> {
                    r1 = leerOperando(im.op2, tablaSimbolos, memoria);
                    idx++;
                }
                case "STORE" -> {
                    Integer dir = tablaSimbolos.obtenerDireccion(im.op1);
                    if (dir != null) memoria.put(dir, r1);
                    idx++;
                }
                case "ADD" -> {
                    r1 += leerOperando(im.op2, tablaSimbolos, memoria);
                    idx++;
                }
                case "SUB" -> {
                    r1 -= leerOperando(im.op2, tablaSimbolos, memoria);
                    idx++;
                }
                case "MUL" -> {
                    r1 *= leerOperando(im.op2, tablaSimbolos, memoria);
                    idx++;
                }
                case "DIV" -> {
                    int v = leerOperando(im.op2, tablaSimbolos, memoria);
                    r1 = (v != 0) ? (r1 / v) : 0;
                    idx++;
                }
                case "CMP" -> {
                    int v = leerOperando(im.op2, tablaSimbolos, memoria);
                    flagCmp = Integer.compare(r1, v);
                    idx++;
                }
                case "JMP" -> idx = saltarA(im.op1, tablaSimbolos, mapaPC, idx + 1);
                case "JGT" -> idx = (flagCmp > 0) ? saltarA(im.op1, tablaSimbolos, mapaPC, idx + 1) : idx + 1;
                case "JLT" -> idx = (flagCmp < 0) ? saltarA(im.op1, tablaSimbolos, mapaPC, idx + 1) : idx + 1;
                case "JEQ" -> idx = (flagCmp == 0) ? saltarA(im.op1, tablaSimbolos, mapaPC, idx + 1) : idx + 1;
                case "JNE" -> idx = (flagCmp != 0) ? saltarA(im.op1, tablaSimbolos, mapaPC, idx + 1) : idx + 1;
                case "HALT" -> idx = codigoMaquina.size();
                default -> idx++;
            }
        }

        System.out.println("   [CPU-SIM] Ejecución finalizada en " + ciclos + " ciclos | Registro final R1 = " + r1);

        Map<String, Integer> resultados = new LinkedHashMap<>();
        for (EntradaSimbolo e : tablaSimbolos.getEntradas()) {
            if (!e.segmento.equals("CODIGO") && memoria.containsKey(e.direccion)) {
                resultados.put(e.nombre, memoria.get(e.direccion));
            }
        }
        return resultados;
    }

    private int leerOperando(String op, TablaSimbolosMemoria tabla, Map<Integer, Integer> memoria) {
        if (op.matches("-?\\d+")) {
            return Integer.parseInt(op);
        }
        Integer dir = tabla.obtenerDireccion(op);
        return (dir != null) ? memoria.getOrDefault(dir, 0) : 0;
    }

    private int saltarA(String etiqueta, TablaSimbolosMemoria tabla, Map<Integer, Integer> mapaPC, int fallback) {
        Integer dir = tabla.obtenerDireccion(etiqueta);
        if (dir != null && mapaPC.containsKey(dir)) {
            return mapaPC.get(dir);
        }
        return fallback;
    }
}
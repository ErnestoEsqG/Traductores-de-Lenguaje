/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act13;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class CompiladorMain {
    private static final String LINEA_DOBLE = "=".repeat(100);
    private static final String LINEA_SIMPLE = "-".repeat(100);

    public static void main(String[] args) {
        System.out.println("Camarena Velazquez Alberto Dario, Esqueda Guzman Ernesto, Nava Gutierrez Alan Santiago");

        String[] archivos = (args.length > 0)
                ? args
                : new String[] {"src/act13/prueba_valida.js", "src/act13/prueba_invalida.js"};

        System.out.println();
        System.out.println(LINEA_DOBLE);
        System.out.println("  COMPILADOR JAVASCRIPT - ACTIVIDAD 13: CÓDIGO INTERMEDIO DE TRES DIRECCIONES (CONTROL DE FLUJO)");
        System.out.println(LINEA_DOBLE);

        for (String archivo : archivos) {
            compilar(archivo);
        }

        System.out.println("Camarena Velazquez Alberto Dario, Esqueda Guzman Ernesto, Nava Gutierrez Alan Santiago");
    }

    private static void compilar(String rutaArchivo) {
        System.out.println();
        System.out.println(LINEA_DOBLE);
        System.out.println("  ARCHIVO: " + rutaArchivo);
        System.out.println(LINEA_DOBLE);

        List<String> fuente;
        try {
            fuente = Files.readAllLines(Paths.get(rutaArchivo), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            System.out.println("  No se pudo leer el archivo: " + ex.getMessage());
            return;
        }

        seccion("1. CÓDIGO FUENTE");
        for (int i = 0; i < fuente.size(); i++) {
            System.out.printf("  %3d | %s%n", i + 1, fuente.get(i));
        }

        ControladorSemantico sem = new ControladorSemantico(fuente);
        String errorFatal = null;
        try {
            LexicoJava lexico = new LexicoJava(new StringReader(String.join("\n", fuente)));
            lexico.sem = sem;
            ParserJava parser = new ParserJava(lexico);
            parser.sem = sem;
            parser.parse();
        } catch (Exception ex) {
            errorFatal = ex.getMessage();
        }
        sem.finPrograma();

        seccion("2. CÓDIGO INTERMEDIO DE TRES DIRECCIONES");
        sem.getGenerador().imprimir();

        seccion("3. TABLA DE SÍMBOLOS");
        sem.getTabla().imprimir();

        seccion("4. ERRORES DETECTADOS");
        int totalErrores = sem.getErrores().size() + (errorFatal != null ? 1 : 0);
        if (totalErrores == 0) {
            System.out.println("  Ninguno.");
        }
        int numero = 1;
        for (String error : sem.getErrores()) {
            System.out.printf("  %2d. %s%n", numero++, error);
        }
        if (errorFatal != null) {
            System.out.printf("  %2d. %s%n", numero, errorFatal);
        }

        seccion("5. RESUMEN");
        System.out.println("  Instrucciones de tres direcciones : " + sem.getGenerador().getTotalInstrucciones());
        System.out.println("  Etiquetas temporales generadas    : " + sem.getGenerador().getTotalTemporales());
        System.out.println("  Etiquetas de salto generadas      : " + sem.getGenerador().getTotalEtiquetas());
        System.out.println("  Símbolos declarados               : " + sem.getTabla().getHistorialSimbolos().size());
        System.out.println("  Errores                           : " + totalErrores);
        System.out.println("  Resultado                         : "
                + (totalErrores == 0 ? "COMPILACIÓN EXITOSA" : "COMPILACIÓN CON ERRORES"));
        System.out.println();
    }

    private static void seccion(String titulo) {
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println("  " + titulo);
        System.out.println(LINEA_SIMPLE);
    }
}

/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act12;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Genera ParserJava.java y sym.java a partir de js_semantico.cup (ejecutar desde la raíz del proyecto). */
public class GeneradorCupSintantico {
    private static final String ENCABEZADO =
            "/*\n"
            + " * Camarena Velazquez Alberto Dario\n"
            + " * Esqueda Guzman Ernesto\n"
            + " * Nava Gutierrez Alan Santiago\n"
            + " */\n";

    public static void main(String[] args) {
        try {
            String[] parametros = {
                "-destdir", "src/act12",
                "-parser", "ParserJava",
                "src/act12/js_semantico.cup"
            };
            java_cup.Main.main(parametros);
            agregarEncabezado(Paths.get("src/act12/ParserJava.java"));
            agregarEncabezado(Paths.get("src/act12/sym.java"));
            System.out.println("ParserJava y sym generados con éxito en src/act12.");
        } catch (Exception ex) {
            System.err.println("Error en JavaCUP: " + ex.getMessage());
        }
    }

    /** CUP no copia comentarios de la gramática: se antepone el encabezado del equipo. */
    private static void agregarEncabezado(Path archivo) throws Exception {
        String contenido = new String(Files.readAllBytes(archivo), StandardCharsets.UTF_8);
        if (!contenido.startsWith(ENCABEZADO)) {
            Files.write(archivo, (ENCABEZADO + contenido).getBytes(StandardCharsets.UTF_8));
        }
    }
}

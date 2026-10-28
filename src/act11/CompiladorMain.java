package act11;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

public class CompiladorMain {
    public static void main(String[] args) {
        // Por defecto ejecuta prueba_valida.js; puedes pasar "src/act11/prueba_invalida.js" como argumento
        String rutaArchivo = (args.length > 0) ? args[0] : "src/act11/prueba_invalida.js";
        System.out.println("\n=== COMPILADOR JAVASCRIPT - ACTIVIDAD 11 ===");
        System.out.println("Archivo a analizar: " + rutaArchivo + "\n");

        try {
            Reader lector = new BufferedReader(new FileReader(rutaArchivo));
            LexicoJava scanner = new LexicoJava(lector);
            ParserJava parser = new ParserJava(scanner);
            parser.parse();
        } catch (Exception ex) {
            System.err.println("Error durante el análisis: " + ex.getMessage());
        }
    }
}
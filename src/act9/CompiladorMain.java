package act9;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

public class CompiladorMain {
    public static void main(String[] args) {
        // Alternar entre "src/act9/prueba_valida.txt" y "src/act9/prueba_invalida.txt"
        String rutaArchivo = "src/act9/prueba_invalida.txt";

        System.out.println("=== ANALIZANDO: " + rutaArchivo + " ===");
        try {
            Reader lector = new BufferedReader(new FileReader(rutaArchivo));
            LexicoJava scanner = new LexicoJava(lector);
            ParserJava parser = new ParserJava(scanner);

            parser.parse();
            System.out.println("=== PROCESAMIENTO FINALIZADO ===");
        } catch (Exception e) {
            System.err.println("Excepción durante la ejecución: " + e.getMessage());
        }
    }
}

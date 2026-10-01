package act8;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

public class CompiladorMain {
    public static void main(String[] args) {
        // Cambiar por "src/act8/prueba_invalida.txt" para probar errores y recuperación
        String rutaArchivo = "src/act8/prueba_invalida.txt";
        
        System.out.println("=== ANALIZANDO: " + rutaArchivo + " ===");
        try {
            Reader lector = new BufferedReader(new FileReader(rutaArchivo));
            LexicoJava scanner = new LexicoJava(lector);
            ParserJava parser = new ParserJava(scanner);
            
            parser.parse();
            System.out.println("=== FIN DEL ANÁLISIS ===");
        } catch (Exception e) {
            System.err.println("Excepción durante la ejecución: " + e.getMessage());
        }
    }
}
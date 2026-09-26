package act7;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

public class MainAct7 {
    public static void main(String[] args) {
        // Cambiar entre "valido.txt", "invalido.txt" o "mixto.txt" según la prueba a ejecutar
        String nombreArchivo = "mixto.txt";
        String rutaArchivo = "src/act7/" + nombreArchivo;

        System.out.println("==================================================");
        System.out.println("EJECUTANDO ANÁLISIS SINTÁCTICO: " + nombreArchivo);
        System.out.println("==================================================");

        try {
            Reader lector = new BufferedReader(new FileReader(rutaArchivo));
            LexicoAct7 lexer = new LexicoAct7(lector);
            ParserAct7 parser = new ParserAct7(lexer);
            parser.parse();
            System.out.println(">> Análisis concluido.");
        } catch (Exception ex) {
            System.err.println(">> Finalización con excepciones: " + ex.getMessage());
        }
    }
}
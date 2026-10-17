package act10;

import java.io.FileReader;

public class CompiladorMain {

    public static void analizar(String rutaArchivo) {
        System.out.println("==================================================");
        System.out.println("Analizando: " + rutaArchivo);
        System.out.println("==================================================");
        ManejadorTipos.reiniciarErrores();

        try {
            FileReader lector = new FileReader(rutaArchivo);
            LexicoJava lexer = new LexicoJava(lector);
            ParserJava parser = new ParserJava(lexer);

            parser.parse();

            if (ManejadorTipos.getTotalErrores() == 0) {
                System.out.println("\n>> ÉXITO: 0 errores semánticos detectados.");
            } else {
                System.out.println("\n>> ANÁLISIS FINALIZADO: Se encontraron " 
                    + ManejadorTipos.getTotalErrores() + " error(es) semántico(s).");
            }
        } catch (Exception e) {
            System.err.println("Excepción durante la ejecución: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        analizar("src/act10/prueba_valida.js");
        System.out.println("\n");
        analizar("src/act10/prueba_invalida.js");
    }
}
/*
PrimerApellido_SegundoApellido_Nombre(s)
*/
package lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java_cup.runtime.Symbol;

public class AnalizadorLexico {
    public static void main(String[] args) {
        // Primera instrucción obligatoria
        System.out.println("==========================================================================");
        System.out.println("INTEGRANTES: [PrimerApellido SegundoApellido Nombre(s)]");
        System.out.println("==========================================================================");
        System.out.println(">>> INICIO DEL ANÁLISIS LÉXICO (JAVASCRIPT) <<<\n");

        String rutaArchivo = "src/lexico/tokens_validos.txt";

        try (Reader lector = new BufferedReader(new FileReader(rutaArchivo))) {
            LexicoJava lexer = new LexicoJava(lector);
            Symbol token;
            while ((token = lexer.next_token()).sym != sym.EOF) {
                // Los tokens reconocidos se imprimen desde LexicoJava
            }
            System.out.println("\n>>> ANÁLISIS LÉXICO FINALIZADO CON ÉXITO <<<");
        } catch (Exception ex) {
            System.err.println("Error durante la lectura del archivo: " + ex.getMessage());
        }

        // Última instrucción obligatoria
        System.out.println("\n==========================================================================");
        System.out.println("INTEGRANTES: [PrimerApellido SegundoApellido Nombre(s)]");
        System.out.println("==========================================================================");
    }
}
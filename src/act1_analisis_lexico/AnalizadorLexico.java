/*
EsquedaGuzman_Ernesto
*/
package act1_analisis_lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java_cup.runtime.Symbol;

public class AnalizadorLexico {
    public static void main(String[] args) {
        // Primera instrucción obligatoria requerida por la práctica
        System.out.println("==========================================================================");
        System.out.println("INTEGRANTES: Esqueda Guzmán Ernesto");
        System.out.println("==========================================================================");
        System.out.println(">>> INICIO DEL ANÁLISIS LÉXICO <<<\n");

        String rutaArchivo = "src/act1_analisis_lexico/tokens_validos.txt";

        try (Reader lector = new BufferedReader(new FileReader(rutaArchivo))) {
            LexicoJava lexer = new LexicoJava(lector);
            while (true) {
                Symbol token = lexer.next_token();
                if (token.sym == sym.EOF) {
                    System.out.println("\n>>> ANÁLISIS LÉXICO FINALIZADO CON ÉXITO <<<");
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("Error durante la lectura del archivo: " + ex.getMessage());
        }

        // Última instrucción obligatoria requerida por la práctica
        System.out.println("\n==========================================================================");
        System.out.println("INTEGRANTES: Esqueda Guzmán Ernesto");
        System.out.println("==========================================================================");
    }
}
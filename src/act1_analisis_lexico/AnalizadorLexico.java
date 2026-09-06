/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act1_analisis_lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java_cup.runtime.Symbol;

public class AnalizadorLexico {
    public static void main(String[] args) {
        System.out.println("Camarena Velazquez Alberto Dario, Esqueda Guzman Ernesto, Nava Gutierrez Alan Santiago"); 
        
        String rutaArchivo = "src/act1_analisis_lexico/tokens_validos.txt";
        try (Reader lector = new BufferedReader(new FileReader(rutaArchivo))) {
            LexicoJava lexer = new LexicoJava(lector);
            while (true) {
                Symbol token = lexer.next_token();
                if (token.sym == sym.EOF) {
                    System.out.println("ANÁLISIS LÉXICO FINALIZADO CON ÉXITO");
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }

        System.out.println("Camarena Velazquez Alberto Dario, Esqueda Guzman Ernesto, Nava Gutierrez Alan Santiago"); 
    }
}

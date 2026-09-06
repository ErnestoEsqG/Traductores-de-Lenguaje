/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act2_analisis_sintactico;

import java.io.BufferedReader;
import java.io.FileReader;
import act1_analisis_lexico.LexicoJava;
import java_cup.runtime.DefaultSymbolFactory;
import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;

public class AnalizadoresSintactico {
    public static void main(String[] args) {
        // Primera instrucción obligatoria con los nombres en orden alfabético
        System.out.println("==========================================================================");
        System.out.println("INTEGRANTES: Camarena Velazquez Alberto Dario, Esqueda Guzman Ernesto, Nava Gutierrez Alan Santiago");
        System.out.println("==========================================================================");
        System.out.println(">>> INICIO DEL ANÁLISIS SINTÁCTICO <<<\n");

        try {
            // Ruta del archivo de prueba con código fuente estructurado
            BufferedReader lector = new BufferedReader(new FileReader("src/act2_analisis_sintactico/sintactico_valido.txt"));
            
            LexicoJava scanner = new LexicoJava(lector);
            ParserSintactico parser = new ParserSintactico(
                new ScannerAdaptador(scanner), new DefaultSymbolFactory());
            
            // Ejecutamos el análisis sintáctico formal de la estructura del archivo
            parser.parse();
            
            System.out.println("\n¡Análisis sintáctico finalizado con éxito!");
        } catch (Exception e) {
            System.err.println("Error de análisis sintáctico: " + e.getMessage());
            e.printStackTrace();
        }

        // Última instrucción obligatoria con los nombres en orden alfabético
        System.out.println("\n==========================================================================");
        System.out.println("INTEGRANTES: Camarena Velazquez Alberto Dario, Esqueda Guzman Ernesto, Nava Gutierrez Alan Santiago");
        System.out.println("==========================================================================");
    }

    // Adaptador de tokens para sincronizar el lexer con el parser sintáctico
    private static class ScannerAdaptador implements Scanner {
        private final LexicoJava scanner;

        ScannerAdaptador(LexicoJava scanner) {
            this.scanner = scanner;
        }

        @Override
        public Symbol next_token() throws Exception {
            Symbol token = scanner.next_token();
            return new Symbol(convertirToken(token.sym), token.left, token.right, token.value);
        }

        private int convertirToken(int token) {
            switch (token) {
                case act1_analisis_lexico.sym.PACKAGE: return sym.PACKAGE;
                case act1_analisis_lexico.sym.IMPORT: return sym.IMPORT;
                case act1_analisis_lexico.sym.CLASS: return sym.CLASS;
                case act1_analisis_lexico.sym.PUBLIC: return sym.PUBLIC;
                case act1_analisis_lexico.sym.IDENTIFIER: return sym.IDENTIFIER;
                case act1_analisis_lexico.sym.DOT: return sym.DOT;
                case act1_analisis_lexico.sym.SEMICOLON: return sym.SEMICOLON;
                case act1_analisis_lexico.sym.MULT: return sym.MULT;
                case act1_analisis_lexico.sym.LBRACE: return sym.LBRACE;
                case act1_analisis_lexico.sym.RBRACE: return sym.RBRACE;
                case act1_analisis_lexico.sym.EOF: return sym.EOF;
                default: return sym.error;
            }
        }
    }
}
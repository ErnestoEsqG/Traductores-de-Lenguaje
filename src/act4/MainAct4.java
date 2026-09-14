package act4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.Scanner;

public class MainAct4 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("\n==========================================");
            System.out.println("  ANALIZADOR SINTÁCTICO - ACTIVIDAD 4     ");
            System.out.println("==========================================");
            System.out.println("1. Ejecutar Programa Válido");
            System.out.println("2. Ejecutar Programa con Errores Recuperables");
            System.out.println("3. Ejecutar Programa con Error Fatal");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        analizar("src/act4/programa_valido.txt");
                        break;
                    case 2:
                        analizar("src/act4/programa_errores.txt");
                        break;
                    case 3:
                        analizar("src/act4/programa_fatal.txt");
                        break;
                    case 4:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        } while (opcion != 4);
    }

    private static void analizar(String rutaArchivo) {
        System.out.println("\n--- Analizando: " + rutaArchivo + " ---");
        try {
            Reader lector = new BufferedReader(new FileReader(rutaArchivo));
            LexicoAct4 lexer = new LexicoAct4(lector);
            ParserAct4 parser = new ParserAct4(lexer);

            parser.parse();

            if (!parser.huboErrorFatal()) {
                System.out.println(">>> Estado: Análisis completado con éxito o recuperado.");
            } else {
                System.out.println(">>> Estado: Análisis detenido por error fatal.");
            }
        } catch (Exception e) {
            System.err.println("Excepción durante la ejecución: " + e.getMessage());
        }
    }
}
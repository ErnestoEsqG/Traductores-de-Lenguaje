package act5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.Scanner;

public class MainAct5 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("\n==========================================");
            System.out.println("  ANALIZADOR SINTÁCTICO - ACTIVIDAD 5     ");
            System.out.println("==========================================");
            System.out.println("1. Ejecutar Código Válido (Atributos y Constantes)");
            System.out.println("2. Ejecutar Código con Errores Sintácticos");
            System.out.println("3. Ejecutar Código Mixto (Recuperación)");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        analizar("src/act5/valido.txt");
                        break;
                    case 2:
                        analizar("src/act5/invalido.txt");
                        break;
                    case 3:
                        analizar("src/act5/mixto.txt");
                        break;
                    case 4:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número entero válido.");
            }
        } while (opcion != 4);
    }

    private static void analizar(String rutaArchivo) {
        System.out.println("\n--- Analizando archivo: " + rutaArchivo + " ---");
        try {
            Reader lector = new BufferedReader(new FileReader(rutaArchivo));
            LexicoAct5 lexer = new LexicoAct5(lector);
            ParserAct5 parser = new ParserAct5(lexer);

            parser.parse();

            if (!parser.huboErrorFatal()) {
                System.out.println(">>> Estado: Análisis completado.");
            } else {
                System.out.println(">>> Estado: Proceso detenido por error fatal.");
            }
        } catch (Exception e) {
            System.err.println("Excepción durante el análisis: " + e.getMessage());
        }
    }
}
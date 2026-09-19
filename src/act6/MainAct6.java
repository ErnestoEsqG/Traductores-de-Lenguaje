package act6;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class MainAct6 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("\n========== MENÚ DE PRUEBAS (JAVASCRIPT) ==========");
            System.out.println("1. Probar archivo válido (valido.txt)");
            System.out.println("2. Probar archivo inválido (invalido.txt)");
            System.out.println("3. Probar archivo mixto (mixto.txt)");
            System.out.println("4. Salir");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        analizar("src/act6/valido.txt");
                        break;
                    case 2:
                        analizar("src/act6/invalido.txt");
                        break;
                    case 3:
                        analizar("src/act6/mixto.txt");
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
        try (BufferedReader lector = new BufferedReader(new FileReader(rutaArchivo))) {
            LexicoAct6 lexer = new LexicoAct6(lector);
            ParserAct6 parser = new ParserAct6(lexer);

            parser.parse();

            if (!parser.huboErrorFatal()) {
                System.out.println(">>> Estado: Análisis completado sin errores.");
            } else {
                System.out.println(">>> Estado: Análisis finalizado con errores sintácticos detectados.");
            }
        } catch (Exception e) {
            System.err.println("Excepción durante el análisis: " + e.getMessage());
        }
    }
}
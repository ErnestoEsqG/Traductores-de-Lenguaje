package act1_analisis_lexico;

import jflex.Main;

public class GeneradorJFlexLexico {
    public static void main(String[] args) {
        try {
            // Actualizamos la ruta a la nueva ubicación del archivo jflex
            String rutaflex = "src/act1_analisis_lexico/js_espanol.jflex";
            
            String datos[] = {rutaflex};
            
            Main.generate(datos);
            System.out.println("¡Analizador léxico generado con éxito!");
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }
}
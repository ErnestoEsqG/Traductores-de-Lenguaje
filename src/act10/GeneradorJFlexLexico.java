package act10;

import jflex.Main;

public class GeneradorJFlexLexico {
    public static void main(String[] args) {
        try {
            String rutaflex = "src/act10/js_semantico.jflex";
            String datos[] = {rutaflex};
            Main.generate(datos);
            System.out.println("Lexer generado exitosamente en act10.");
        } catch (Exception ex) {
            System.err.println("Error al generar Lexer: " + ex.getMessage());
        }
    }
}
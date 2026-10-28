package act11;

import jflex.Main;

public class GeneradorJFlexLexico {
    public static void main(String[] args) {
        try {
            String rutaflex = "src/act11/js_semantico.jflex";
            String[] datos = {rutaflex};
            Main.generate(datos);
            System.out.println("LexicoJava generado con éxito en src/act11.");
        } catch (Exception ex) {
            System.err.println("Error en JFlex: " + ex.getMessage());
        }
    }
}
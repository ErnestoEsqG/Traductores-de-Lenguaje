package act8;

import jflex.Main;

public class GeneradorJFlexLexico {
    public static void main(String[] args) {
        try {
            String rutaflex = "src/act8/js_sintactico.jflex";
            String[] datos = {rutaflex};
            Main.generate(datos);
            System.out.println("LexicoJava.java generado exitosamente en act8.");
        } catch (Exception ex) {
            System.err.println("Error en JFlex: " + ex.getMessage());
        }
    }
}
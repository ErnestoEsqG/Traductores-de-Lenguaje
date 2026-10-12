package act9;

import jflex.Main;

public class GeneradorJFlexLexico {
    public static void main(String[] args) {
        try {
            String rutaflex = "src/act9/js_tabla.jflex";
            String[] datos = {rutaflex};
            Main.generate(datos);
            System.out.println("LexicoJava.java generado exitosamente para act9.");
        } catch (Exception ex) {
            System.err.println("Error en JFlex: " + ex.getMessage());
        }
    }
}
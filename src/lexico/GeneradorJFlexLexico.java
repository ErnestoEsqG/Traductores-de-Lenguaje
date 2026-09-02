package lexico;

import jflex.Main;

public class GeneradorJFlexLexico {
    public static void main(String[] args) {
        try {
            String rutaflex = "src/lexico/js_espanol.jflex";
            String[] datos = {rutaflex};
            Main.generate(datos);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }
}
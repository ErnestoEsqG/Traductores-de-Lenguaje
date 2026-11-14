/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act13;

/** Genera LexicoJava.java a partir de js_semantico.jflex (ejecutar desde la raíz del proyecto). */
public class GeneradorJFlexLexico {
    public static void main(String[] args) {
        try {
            String[] datos = {"src/act13/js_semantico.jflex"};
            jflex.Main.generate(datos);
            System.out.println("LexicoJava generado con éxito en src/act13.");
        } catch (Exception ex) {
            System.err.println("Error en JFlex: " + ex.getMessage());
        }
    }
}

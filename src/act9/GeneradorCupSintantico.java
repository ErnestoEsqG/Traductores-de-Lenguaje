package act9;

public class GeneradorCupSintantico {
    public static void main(String[] args) {
        try {
            String[] parametros = {
                "-destdir", "src/act9",
                "-parser", "ParserJava", 
                "-symbols", "sym",
                "src/act9/js_tabla.cup"
            };
            java_cup.Main.main(parametros);
            System.out.println("ParserJava.java y sym.java generados exitosamente para act9.");
        } catch (Exception ex) {
            System.err.println("Error en CUP: " + ex.getMessage());
        }
    }
}
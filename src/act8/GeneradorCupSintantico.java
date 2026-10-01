package act8;

public class GeneradorCupSintantico {
    public static void main(String[] args) {
        try {
            String[] parametros = {
                "-destdir", "src/act8",
                "-parser", "ParserJava", 
                "-symbols", "sym",
                "src/act8/js_sintactico.cup"
            };
            java_cup.Main.main(parametros);
            System.out.println("ParserJava.java y sym.java generados exitosamente en act8.");
        } catch (Exception ex) {
            System.err.println("Error en CUP: " + ex.getMessage());
        }
    }
}
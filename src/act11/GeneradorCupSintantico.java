package act11;

public class GeneradorCupSintantico {
    public static void main(String[] args) {
        try {
            String[] parametros = {
                "-destdir", "src/act11",
                "-parser", "ParserJava",
                "-progress", "src/act11/js_semantico.cup"
            };
            java_cup.Main.main(parametros);
            System.out.println("ParserJava y sym generados con éxito en src/act11.");
        } catch (Exception ex) {
            System.err.println("Error en JavaCUP: " + ex.getMessage());
        }
    }
}
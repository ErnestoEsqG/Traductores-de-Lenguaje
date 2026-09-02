package lexico;

public class GeneradorCupSintantico {
    public static void main(String[] args) throws Exception {
        String[] parametros = {
            "-destdir", "src/lexico",
            "-parser", "ParserJava", 
            "-progress", "src/lexico/js_espanol.cup"
        };
        java_cup.Main.main(parametros);
    }
}
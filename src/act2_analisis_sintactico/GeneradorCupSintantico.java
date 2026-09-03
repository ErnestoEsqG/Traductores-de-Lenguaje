package act2_analisis_sintactico;

import java.io.IOException;

public class GeneradorCupSintantico {
    public static void main(String[] args) throws IOException, Exception {
        String[] parametros = {
            "-destdir", "src/act2_analisis_sintactico",
            "-parser", "ParserSintactico", 
            "-progress", "src/act2_analisis_sintactico/js_espanol.cup"
        };
        java_cup.Main.main(parametros);
        System.out.println("¡Parser sintáctico y símbolos generados con éxito en src/act2_analisis_sintactico!");
    }
}
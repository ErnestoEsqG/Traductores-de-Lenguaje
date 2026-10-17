package act10;

import java.io.IOException;

public class GeneradorCupSintantico {
    public static void main(String[] args) throws IOException, Exception {
        String[] parametros = {
            "-destdir", "src/act10",
            "-parser", "ParserJava", 
            "-progress", "src/act10/js_semantico.cup"
        };
        java_cup.Main.main(parametros);
        System.out.println("Parser y sym generados exitosamente en act10.");
    }
}
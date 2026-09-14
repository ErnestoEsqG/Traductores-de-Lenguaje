package act4;

import java.io.File;

public class GeneradorAct4 {
    public static void main(String[] args) {
        try {
            // 1. Generar LexicoAct4.java con JFlex
            String rutaFlex = "src" + File.separator + "act4" + File.separator + "act4.jflex";
            String[] paramsFlex = { rutaFlex };
            jflex.Main.generate(paramsFlex);

            // 2. Generar ParserAct4.java y sym.java con CUP
            String rutaDestino = "src" + File.separator + "act4";
            String rutaCup = "src" + File.separator + "act4" + File.separator + "act4.cup";
            String[] paramsCup = {
                "-destdir", rutaDestino,
                "-parser", "ParserAct4",
                "-symbols", "sym",
                rutaCup
            };
            java_cup.Main.main(paramsCup);

            System.out.println(">>> LexicoAct4, ParserAct4 y sym generados exitosamente en act4.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
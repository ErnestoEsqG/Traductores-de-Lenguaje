package act5;

import java.io.File;

public class GeneradorAct5 {
    public static void main(String[] args) {
        try {
            String rutaFlex = "src" + File.separator + "act5" + File.separator + "act5.jflex";
            jflex.Main.generate(new String[]{rutaFlex});

            String[] paramsCup = {
                "-destdir", "src" + File.separator + "act5",
                "-parser", "ParserAct5",
                "-symbols", "sym",
                "src" + File.separator + "act5" + File.separator + "act5.cup"
            };
            java_cup.Main.main(paramsCup);

            System.out.println(">>> LexicoAct5, ParserAct5 y sym generados exitosamente en act5.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
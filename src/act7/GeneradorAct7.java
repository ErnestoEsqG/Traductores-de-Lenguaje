package act7;

import java.io.File;

public class GeneradorAct7 {
    public static void main(String[] args) {
        try {
            String destinoDir = "src" + File.separator + "act7";
            String rutaFlex = destinoDir + File.separator + "act7.jflex";
            String rutaCup = destinoDir + File.separator + "act7.cup";

            System.out.println("Generando archivos con JavaCUP...");
            String[] paramsCup = {
                "-destdir", destinoDir,
                "-parser", "ParserAct7",
                "-symbols", "sym",
                "-expect", "2",
                "-progress", rutaCup
            };
            java_cup.Main.main(paramsCup);

            System.out.println("Generando analizador léxico con JFlex...");
            String[] paramsFlex = { rutaFlex };
            jflex.Main.generate(paramsFlex);

            System.out.println(">> ¡Generación completada con éxito!");
        } catch (Exception e) {
            System.err.println("Error al compilar gramáticas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
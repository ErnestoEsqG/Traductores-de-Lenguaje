package act6;

public class GeneradorAct6 {
    public static void main(String[] args) {
        try {
            // 1. Generar Lexico con JFlex
            String[] flexParams = {"src/act6/act6.jflex"};
            jflex.Main.generate(flexParams);

            // 2. Generar Parser y sym con CUP
            String[] cupParams = {
                "-destdir", "src/act6",
                "-parser", "ParserAct6",
                "-symbols", "sym",
                "src/act6/act6.cup"
            };
            java_cup.Main.main(cupParams);
            
            System.out.println("\n>>> Archivos LexicoAct6.java, ParserAct6.java y sym.java generados con éxito.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act14;

import java.util.*;

public class CompiladorMain {

    private static final String INTEGRANTES =
            "Camarena Velazquez Alberto Dario | Esqueda Guzman Ernesto | Nava Gutierrez Alan Santiago";

    public static void main(String[] args) {
        // Primera instrucción obligatoria: nombres completos en orden alfabético
        System.out.println("==========================================================================");
        System.out.println("INTEGRANTES: " + INTEGRANTES);
        System.out.println("ACTIVIDAD 14: OPTIMIZACION DE CODIGO INTERMEDIO (TAC)");
        System.out.println("==========================================================================\n");

        OptimizadorCodigoIntermedio optimizador = new OptimizadorCodigoIntermedio();
        EvaluadorTAC evaluador = new EvaluadorTAC();

        // CASO 1: Regla 1A - Instrucciones inalcanzables después de salto incondicional
        ejecutarPrueba(
                "CASO 1: Eliminación de Instrucciones Inalcanzables (Código Muerto)",
                List.of(
                        "L1:",
                        "x = 10",
                        "goto L2",
                        "y = 20",
                        "L2:",
                        "z = x + y"
                ),
                Map.of("y", 5),
                Set.of("z"),
                optimizador, evaluador
        );

        // CASO 2: Regla 1B - Variables asignadas pero no utilizadas
        ejecutarPrueba(
                "CASO 2: Eliminación de Variables No Utilizadas (Código Muerto)",
                List.of(
                        "a = 5",
                        "b = 10",
                        "c = b + 2",
                        "result = c"
                ),
                Map.of(),
                Set.of("result"),
                optimizador, evaluador
        );

        // CASO 3: Regla 2A - Eliminación de cálculos repetidos (CSE)
        ejecutarPrueba(
                "CASO 3: Eliminación de Cálculos Repetidos (CSE)",
                List.of(
                        "t1 = a + b",
                        "t2 = a + b",
                        "result = t1"
                ),
                Map.of("a", 7, "b", 3),
                Set.of("result"),
                optimizador, evaluador
        );

        // CASO 4: Regla 2B - Eliminación de asignaciones innecesarias / sobrescritas
        ejecutarPrueba(
                "CASO 4: Eliminación de Asignaciones Innecesarias y Propagación",
                List.of(
                        "temp = a",
                        "temp = b",
                        "result = temp"
                ),
                Map.of("a", 100, "b", 42),
                Set.of("result"),
                optimizador, evaluador
        );

        // CASO 5: Regla 3 - Preservación de lógica en estructuras de control (if-else)
        ejecutarPrueba(
                "CASO 5: Reglas Sistemáticas en Control de Flujo (if-else)",
                List.of(
                        "a = 5",
                        "basura = 999",
                        "if a > 10 goto L1",
                        "b = a + 2",
                        "goto L2",
                        "muerto = 123",
                        "L1:",
                        "b = a * 2",
                        "L2:",
                        "c = b + 1"
                ),
                Map.of(),
                Set.of("c"),
                optimizador, evaluador
        );

        // CASO 6: Punto 4 - Simplificación algebraica y validación de equivalencia
        ejecutarPrueba(
                "CASO 6: Simplificación de Cálculos Encadenados y Validación",
                List.of(
                        "t1 = x + 5",
                        "t2 = t1 + 10",
                        "result = t2"
                ),
                Map.of("x", 10),
                Set.of("result"),
                optimizador, evaluador
        );

        // Última instrucción obligatoria: nombres completos en orden alfabético
        System.out.println("==========================================================================");
        System.out.println("INTEGRANTES: " + INTEGRANTES);
        System.out.println("FIN DE LA EJECUCION - ACTIVIDAD 14");
        System.out.println("==========================================================================");
    }

    private static void ejecutarPrueba(
            String titulo,
            List<String> codigoOriginal,
            Map<String, Integer> entradas,
            Set<String> variablesSalida,
            OptimizadorCodigoIntermedio optimizador,
            EvaluadorTAC evaluador) {

        System.out.println("--------------------------------------------------------------------------");
        System.out.println(titulo);
        System.out.println("--------------------------------------------------------------------------");

        System.out.println("1) Código Intermedio Original (" + codigoOriginal.size() + " instrucciones):");
        imprimirCodigo(codigoOriginal);

        List<String> codigoOptimizado = optimizador.optimizar(codigoOriginal, variablesSalida);

        System.out.println("\n2) Reglas Aplicadas durante la Optimización:");
        List<String> cambios = optimizador.getRegistroCambios();
        if (cambios.isEmpty()) {
            System.out.println("   (Sin cambios necesarios)");
        } else {
            for (String c : cambios) {
                System.out.println("   - " + c);
            }
        }

        System.out.println("\n3) Código Intermedio Optimizado (" + codigoOptimizado.size() + " instrucciones):");
        imprimirCodigo(codigoOptimizado);

        int eliminadas = codigoOriginal.size() - codigoOptimizado.size();
        double porcentaje = codigoOriginal.isEmpty() ? 0 : (eliminadas * 100.0) / codigoOriginal.size();
        System.out.printf("\n   [Métrica] Reducción: %d instrucciones eliminadas (%.1f%%)%n", eliminadas, porcentaje);

        Map<String, Integer> resOriginal = evaluador.ejecutar(codigoOriginal, entradas);
        Map<String, Integer> resOptimizado = evaluador.ejecutar(codigoOptimizado, entradas);

        System.out.println("\n4) Validación de Equivalencia (Entradas: " + entradas + "):");
        boolean todoCorrecto = true;
        for (String var : variablesSalida) {
            Integer vOrig = resOriginal.get(var);
            Integer vOpt = resOptimizado.get(var);
            boolean coincide = Objects.equals(vOrig, vOpt);
            todoCorrecto &= coincide;
            System.out.printf("   Variable '%s' -> Original: %s | Optimizado: %s -> %s%n",
                    var, vOrig, vOpt, coincide ? "CORRECTO" : "ERROR");
        }
        System.out.println("   Estado de validación: " + (todoCorrecto ? "[OK] Mismo resultado garantizado" : "[FALLO] Revisar reglas"));
        System.out.println();
    }

    private static void imprimirCodigo(List<String> codigo) {
        for (int i = 0; i < codigo.size(); i++) {
            System.out.printf("   %02d | %s%n", i + 1, codigo.get(i));
        }
    }
}
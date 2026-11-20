/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act15;

import act15.GeneradorCodigoMaquina.InstruccionMaquina;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class CompiladorMain {

    private static final String INTEGRANTES =
            "Camarena Velazquez Alberto Dario | Esqueda Guzman Ernesto | Nava Gutierrez Alan Santiago";

    public static void main(String[] args) {
        // Primera instrucción obligatoria: nombres completos en orden alfabético
        System.out.println("==============================================================================");
        System.out.println("INTEGRANTES: " + INTEGRANTES);
        System.out.println("ACTIVIDAD 15: GENERACION DE CODIGO EJECUTABLE");
        System.out.println("==============================================================================\n");

        GeneradorCodigoMaquina generador = new GeneradorCodigoMaquina();
        ConstructorEjecutable constructor = new ConstructorEjecutable();

        // CASO 1: Mapeo de instrucciones TAC a código máquina (ejemplo 1 de la práctica)
        ejecutarCaso(
                "CASO 1: Mapeo de Código Intermedio a Código Máquina",
                "programa_caso1.exec",
                List.of(
                        "t1 = a + b",
                        "t2 = t1 * c",
                        "result = t2"
                ),
                Map.of("a", 4, "b", 6, "c", 3),
                Set.of("result"),
                generador, constructor
        );

        // CASO 2: Optimización de instrucciones en operaciones encadenadas (ejemplo 2)
        ejecutarCaso(
                "CASO 2: Optimización de Instrucciones de Máquina (Peephole)",
                "programa_caso2.exec",
                List.of(
                        "t1 = a + b",
                        "t2 = t1 + c",
                        "result = t2"
                ),
                Map.of("a", 10, "b", 20, "c", 5),
                Set.of("result"),
                generador, constructor
        );

        // CASO 3: Asignación de direcciones con etiquetas y tabla de símbolos (ejemplo 3)
        ejecutarCaso(
                "CASO 3: Asignación de Direcciones de Memoria y Tabla de Símbolos",
                "programa_caso3.exec",
                List.of(
                        "L1:",
                        "t1 = a + b",
                        "L2:",
                        "t2 = t1 * c",
                        "result = t2"
                ),
                Map.of("a", 2, "b", 3, "c", 5),
                Set.of("result"),
                generador, constructor
        );

        // CASO 4: Control de flujo condicional compilado a saltos con direcciones reales
        ejecutarCaso(
                "CASO 4: Control de Flujo Condicional y Ejecución en Entorno Virtual",
                "programa_caso4.exec",
                List.of(
                        "if a > b goto L_MAYOR",
                        "result = b",
                        "goto L_FIN",
                        "L_MAYOR:",
                        "result = a",
                        "L_FIN:"
                ),
                Map.of("a", 15, "b", 10),
                Set.of("result"),
                generador, constructor
        );

        // Última instrucción obligatoria: nombres completos en orden alfabético
        System.out.println("==============================================================================");
        System.out.println("INTEGRANTES: " + INTEGRANTES);
        System.out.println("FIN DE LA EJECUCION - ACTIVIDAD 15");
        System.out.println("==============================================================================");
    }

    private static void ejecutarCaso(
            String titulo,
            String nombreEjecutable,
            List<String> codigoTAC,
            Map<String, Integer> entradas,
            Set<String> variablesSalida,
            GeneradorCodigoMaquina generador,
            ConstructorEjecutable constructor) {

        System.out.println("------------------------------------------------------------------------------");
        System.out.println(titulo);
        System.out.println("------------------------------------------------------------------------------");

        System.out.println("1) Código Intermedio (TAC):");
        for (int i = 0; i < codigoTAC.size(); i++) {
            System.out.printf("   %02d | %s%n", i + 1, codigoTAC.get(i));
        }

        // Paso 1A: Traducción directa sin optimizar
        List<InstruccionMaquina> sinOptimizar = generador.traducirSinOptimizar(codigoTAC);
        long cantSinOpt = sinOptimizar.stream().filter(im -> !im.esEtiqueta()).count();

        // Paso 1B: Optimización peephole
        List<InstruccionMaquina> optimizadas = generador.optimizarInstrucciones(sinOptimizar);
        long cantOpt = optimizadas.stream().filter(im -> !im.esEtiqueta()).count();

        System.out.println("\n2) Traducción y Optimización de Instrucciones:");
        List<String> logOpt = generador.getRegistroOptimizaciones();
        if (logOpt.isEmpty()) {
            System.out.println("   (Sin instrucciones redundantes que eliminar)");
        } else {
            for (String msg : logOpt) {
                System.out.println("   - " + msg);
            }
        }
        System.out.printf("   [Métrica] Instrucciones máquina: %d (sin optimizar) -> %d (optimizado)%n",
                cantSinOpt, cantOpt);

        // Paso 2: Asignación de direcciones y tabla de símbolos
        TablaSimbolosMemoria tablaSimbolos = new TablaSimbolosMemoria();
        List<InstruccionMaquina> codigoFinal = generador.asignarDireccionesYCodificar(
                optimizadas, variablesSalida, tablaSimbolos);

        System.out.println("\n3) Tabla de Símbolos (Direcciones de Memoria Asignadas):");
        tablaSimbolos.imprimirTabla();

        System.out.println("\n4) Código Máquina con Direcciones y Codificación Hexadecimal:");
        for (InstruccionMaquina im : codigoFinal) {
            System.out.println("   " + im.formatoConDirecciones());
        }

        // Paso 3: Creación del archivo ejecutable y ejecución en entorno configurado
        System.out.println("\n5) Preparación de Archivo Ejecutable y Ejecución:");
        try {
            Path archivo = constructor.generarArchivoEjecutable(nombreEjecutable, codigoFinal, tablaSimbolos);
            Map<String, Integer> memoriaFinal = constructor.configurarYEjecutar(
                    archivo, codigoFinal, tablaSimbolos, entradas);

            System.out.println("   Entradas iniciales : " + entradas);
            for (String var : variablesSalida) {
                System.out.printf("   Resultado en memoria [%s (%s)] = %s%n",
                        var, tablaSimbolos.obtenerDireccionHex(var), memoriaFinal.get(var));
            }
        } catch (IOException e) {
            System.err.println("   [ERROR] No se pudo generar el archivo ejecutable: " + e.getMessage());
        }
        System.out.println();
    }
}
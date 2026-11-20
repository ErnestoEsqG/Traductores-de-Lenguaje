/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act15;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneradorCodigoMaquina {

    public static class InstruccionMaquina {
        public String opcode;      // LOAD, STORE, ADD, SUB, MUL, DIV, CMP, JMP, JGT, JLT, JEQ, HALT, LABEL
        public String op1;         // Registro, variable o etiqueta
        public String op2;         // Operando fuente o destino
        public String comentario;
        public int direccion;      // Dirección en segmento de código (0x1000+)
        public String op1Resuelto; // Dirección hex o registro
        public String op2Resuelto; // Dirección hex o inmediato
        public String codigoHex;   // Palabra de máquina de 32 bits

        public InstruccionMaquina(String opcode, String op1, String op2, String comentario) {
            this.opcode = opcode;
            this.op1 = op1;
            this.op2 = op2;
            this.comentario = comentario;
        }

        public boolean esEtiqueta() {
            return "LABEL".equals(opcode);
        }

        public String formatoSimbolico() {
            if (esEtiqueta()) return op1 + ":";
            if ("HALT".equals(opcode)) return "HALT";
            if (op2 == null || op2.isEmpty()) return String.format("%-6s %s", opcode, op1);
            return String.format("%-6s %s, %s", opcode, op1, op2);
        }

        public String formatoConDirecciones() {
            String dirHex = String.format("0x%04X", direccion);
            if ("HALT".equals(opcode)) {
                return String.format("%s: %-24s | HEX: %s  ; %s", dirHex, "HALT", codigoHex, comentario);
            }
            String cuerpo = (op2Resuelto == null || op2Resuelto.isEmpty())
                    ? String.format("%-6s %s", opcode, op1Resuelto)
                    : String.format("%-6s %s, %s", opcode, op1Resuelto, op2Resuelto);
            return String.format("%s: %-24s | HEX: %s  ; %s", dirHex, cuerpo, codigoHex, comentario);
        }
    }

    private static final Pattern PATRON_ETIQUETA = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*):$");
    private static final Pattern PATRON_GOTO = Pattern.compile("^goto\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");
    private static final Pattern PATRON_IF = Pattern.compile(
            "^if\\s+([a-zA-Z0-9_]+)\\s*(==|!=|<|>)\\s*([a-zA-Z0-9_]+)\\s+goto\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");
    private static final Pattern PATRON_BINARIA = Pattern.compile(
            "^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*([a-zA-Z0-9_]+)\\s*([+\\-*/])\\s*([a-zA-Z0-9_]+)$");
    private static final Pattern PATRON_ASIGNACION = Pattern.compile(
            "^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*([a-zA-Z0-9_]+)$");

    private final List<String> registroOptimizaciones = new ArrayList<>();

    public List<String> getRegistroOptimizaciones() {
        return Collections.unmodifiableList(registroOptimizaciones);
    }

    // =========================================================================
    // PUNTO 1A: Mapeo directo de TAC a Instrucciones de Máquina (sin optimizar)
    // =========================================================================
    public List<InstruccionMaquina> traducirSinOptimizar(List<String> codigoTAC) {
        List<InstruccionMaquina> instrucciones = new ArrayList<>();

        for (String linea : codigoTAC) {
            String inst = linea.trim();
            if (inst.isEmpty()) continue;

            Matcher mEtiq = PATRON_ETIQUETA.matcher(inst);
            if (mEtiq.matches()) {
                instrucciones.add(new InstruccionMaquina("LABEL", mEtiq.group(1), "", "Etiqueta " + mEtiq.group(1)));
                continue;
            }

            Matcher mGoto = PATRON_GOTO.matcher(inst);
            if (mGoto.matches()) {
                instrucciones.add(new InstruccionMaquina("JMP", mGoto.group(1), "", "Salto incondicional a " + mGoto.group(1)));
                continue;
            }

            Matcher mIf = PATRON_IF.matcher(inst);
            if (mIf.matches()) {
                String izq = mIf.group(1);
                String rel = mIf.group(2);
                String der = mIf.group(3);
                String dest = mIf.group(4);
                instrucciones.add(new InstruccionMaquina("LOAD", "R1", izq, "Cargar " + izq + " en R1"));
                instrucciones.add(new InstruccionMaquina("CMP", "R1", der, "Comparar R1 con " + der));
                String opSalto = switch (rel) {
                    case ">" -> "JGT";
                    case "<" -> "JLT";
                    case "==" -> "JEQ";
                    default -> "JNE";
                };
                instrucciones.add(new InstruccionMaquina(opSalto, dest, "", "Saltar a " + dest + " si " + izq + " " + rel + " " + der));
                continue;
            }

            Matcher mBin = PATRON_BINARIA.matcher(inst);
            if (mBin.matches()) {
                String dest = mBin.group(1);
                String op1 = mBin.group(2);
                String operador = mBin.group(3);
                String op2 = mBin.group(4);
                String opMaquina = switch (operador) {
                    case "+" -> "ADD";
                    case "-" -> "SUB";
                    case "*" -> "MUL";
                    case "/" -> "DIV";
                    default -> "ADD";
                };
                instrucciones.add(new InstruccionMaquina("LOAD", "R1", op1, "Cargar " + op1 + " en R1"));
                instrucciones.add(new InstruccionMaquina(opMaquina, "R1", op2, "Operar R1 " + operador + " " + op2));
                instrucciones.add(new InstruccionMaquina("STORE", dest, "R1", "Almacenar R1 en " + dest));
                continue;
            }

            Matcher mAsig = PATRON_ASIGNACION.matcher(inst);
            if (mAsig.matches()) {
                String dest = mAsig.group(1);
                String fuente = mAsig.group(2);
                instrucciones.add(new InstruccionMaquina("LOAD", "R1", fuente, "Cargar " + fuente + " en R1"));
                instrucciones.add(new InstruccionMaquina("STORE", dest, "R1", "Almacenar R1 en " + dest));
            }
        }

        instrucciones.add(new InstruccionMaquina("HALT", "", "", "Fin de ejecución del programa"));
        return instrucciones;
    }

    // =========================================================================
    // PUNTO 1B: Optimización de Instrucciones (Peephole / Reutilización de R1)
    // =========================================================================
    public List<InstruccionMaquina> optimizarInstrucciones(List<InstruccionMaquina> originales) {
        registroOptimizaciones.clear();
        Set<String> etiquetasDestinoSalto = new HashSet<>();
        for (InstruccionMaquina im : originales) {
            if (im.opcode.equals("JMP") || im.opcode.startsWith("J")) {
                etiquetasDestinoSalto.add(im.op1);
            }
        }

        List<InstruccionMaquina> lista = new ArrayList<>();
        for (InstruccionMaquina im : originales) {
            lista.add(new InstruccionMaquina(im.opcode, im.op1, im.op2, im.comentario));
        }

        boolean huboCambios;
        do {
            huboCambios = false;
            for (int i = 0; i < lista.size() - 1; i++) {
                InstruccionMaquina actual = lista.get(i);
                if (!"STORE".equals(actual.opcode) || !actual.op1.matches("t\\d+")) {
                    continue;
                }

                // Buscar la siguiente instrucción real (permitiendo etiquetas que no sean destino de salto)
                int j = i + 1;
                boolean bloqueoPorSalto = false;
                while (j < lista.size() && lista.get(j).esEtiqueta()) {
                    if (etiquetasDestinoSalto.contains(lista.get(j).op1)) {
                        bloqueoPorSalto = true;
                        break;
                    }
                    j++;
                }

                if (!bloqueoPorSalto && j < lista.size()) {
                    InstruccionMaquina sig = lista.get(j);
                    if ("LOAD".equals(sig.opcode) && sig.op1.equals(actual.op2) && sig.op2.equals(actual.op1)) {
                        // Verificar que el temporal no se vuelva a leer más adelante
                        if (!seLeeDespues(lista, j + 1, actual.op1)) {
                            registroOptimizaciones.add(
                                    "[PEEPHOLE] Eliminado par redundante \"" + actual.formatoSimbolico()
                                            + "\" + \"" + sig.formatoSimbolico()
                                            + "\" (valor conservado en " + actual.op2 + ")");
                            lista.remove(j);
                            lista.remove(i);
                            huboCambios = true;
                            break;
                        }
                    }
                }
            }
        } while (huboCambios);

        return lista;
    }

    private boolean seLeeDespues(List<InstruccionMaquina> lista, int desde, String temp) {
        for (int k = desde; k < lista.size(); k++) {
            InstruccionMaquina im = lista.get(k);
            if (!"STORE".equals(im.opcode) && !im.esEtiqueta()) {
                if (temp.equals(im.op1) || temp.equals(im.op2)) return true;
            }
        }
        return false;
    }

    // =========================================================================
    // PUNTO 2: Asignación de Direcciones de Memoria y Codificación Hexadecimal
    // =========================================================================
    public List<InstruccionMaquina> asignarDireccionesYCodificar(
            List<InstruccionMaquina> instrucciones,
            Set<String> variablesSalida,
            TablaSimbolosMemoria tablaSimbolos) {

        List<InstruccionMaquina> ejecutables = new ArrayList<>();
        int dirCodigo = 0x1000;

        // Primera pasada: asignar direcciones a instrucciones y registrar etiquetas y variables
        for (InstruccionMaquina im : instrucciones) {
            if (im.esEtiqueta()) {
                tablaSimbolos.registrarEtiqueta(im.op1, dirCodigo);
                continue;
            }

            im.direccion = dirCodigo;
            dirCodigo += 4; // Instrucciones de longitud fija de 4 bytes (32 bits)

            registrarSiEsVariable(im.op1, variablesSalida, tablaSimbolos, im.opcode);
            registrarSiEsVariable(im.op2, variablesSalida, tablaSimbolos, im.opcode);
            ejecutables.add(im);
        }

        // Segunda pasada: resolver operandos a direcciones hexadecimales y generar opcode de 32 bits
        for (InstruccionMaquina im : ejecutables) {
            im.op1Resuelto = resolverOperando(im.op1, tablaSimbolos);
            im.op2Resuelto = resolverOperando(im.op2, tablaSimbolos);
            im.codigoHex = codificarHex32(im, tablaSimbolos);
        }

        return ejecutables;
    }

    private void registrarSiEsVariable(String op, Set<String> salidas, TablaSimbolosMemoria tabla, String opcode) {
        if (op == null || op.isEmpty()) return;
        if (op.equals("R1") || op.equals("R2") || op.matches("-?\\d+")) return;
        if (opcode.equals("JMP") || opcode.startsWith("J")) return; // Es etiqueta, ya registrada
        tabla.registrarVariable(op, salidas.contains(op));
    }

    private String resolverOperando(String op, TablaSimbolosMemoria tabla) {
        if (op == null || op.isEmpty()) return "";
        if (op.equals("R1") || op.equals("R2") || op.matches("-?\\d+")) return op;
        return tabla.obtenerDireccionHex(op);
    }

    private String codificarHex32(InstruccionMaquina im, TablaSimbolosMemoria tabla) {
        int opNum = switch (im.opcode) {
            case "LOAD"  -> 0x01;
            case "STORE" -> 0x02;
            case "ADD"   -> 0x03;
            case "SUB"   -> 0x04;
            case "MUL"   -> 0x05;
            case "DIV"   -> 0x06;
            case "CMP"   -> 0x07;
            case "JMP"   -> 0x08;
            case "JGT"   -> 0x09;
            case "JLT"   -> 0x0A;
            case "JEQ"   -> 0x0B;
            case "JNE"   -> 0x0C;
            case "HALT"  -> 0xFF;
            default      -> 0x00;
        };

        int reg = ("R1".equals(im.op1) || "R1".equals(im.op2)) ? 0x01 : 0x00;
        int dir = 0x0000;

        String candidatoDir = ("STORE".equals(im.opcode) || im.opcode.startsWith("J")) ? im.op1 : im.op2;
        if (candidatoDir != null && !candidatoDir.isEmpty()) {
            Integer d = tabla.obtenerDireccion(candidatoDir);
            if (d != null) {
                dir = d & 0xFFFF;
            } else if (candidatoDir.matches("-?\\d+")) {
                dir = Integer.parseInt(candidatoDir) & 0xFFFF;
            }
        }
        return String.format("%02X %02X %04X", opNum, reg, dir);
    }
}
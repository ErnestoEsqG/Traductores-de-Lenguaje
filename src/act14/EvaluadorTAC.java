/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act14;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EvaluadorTAC {

    private static final Pattern PATRON_ETIQUETA = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*):$");
    private static final Pattern PATRON_GOTO = Pattern.compile("^goto\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");
    private static final Pattern PATRON_IF = Pattern.compile(
            "^(if|ifFalse)\\s+([a-zA-Z0-9_]+)\\s*(==|!=|<=|>=|<|>)\\s*([a-zA-Z0-9_]+)\\s+goto\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");
    private static final Pattern PATRON_BINARIA = Pattern.compile(
            "^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*([a-zA-Z0-9_]+)\\s*([+\\-*/])\\s*([a-zA-Z0-9_]+)$");
    private static final Pattern PATRON_ASIGNACION = Pattern.compile(
            "^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*([a-zA-Z0-9_]+)$");

    public Map<String, Integer> ejecutar(List<String> codigo, Map<String, Integer> entradasIniciales) {
        Map<String, Integer> memoria = new LinkedHashMap<>(entradasIniciales);
        Map<String, Integer> etiquetas = new HashMap<>();

        for (int i = 0; i < codigo.size(); i++) {
            Matcher m = PATRON_ETIQUETA.matcher(codigo.get(i).trim());
            if (m.matches()) {
                etiquetas.put(m.group(1), i);
            }
        }

        int pc = 0;
        int pasos = 0;
        int maxPasos = 1000;

        while (pc < codigo.size() && pasos < maxPasos) {
            pasos++;
            String inst = codigo.get(pc).trim();

            if (PATRON_ETIQUETA.matcher(inst).matches()) {
                pc++;
                continue;
            }

            Matcher mGoto = PATRON_GOTO.matcher(inst);
            if (mGoto.matches()) {
                pc = etiquetas.getOrDefault(mGoto.group(1), codigo.size());
                continue;
            }

            Matcher mIf = PATRON_IF.matcher(inst);
            if (mIf.matches()) {
                String tipo = mIf.group(1);
                int izq = obtenerValor(mIf.group(2), memoria);
                String rel = mIf.group(3);
                int der = obtenerValor(mIf.group(4), memoria);
                String destino = mIf.group(5);

                boolean condicion = evaluarRelacional(izq, rel, der);
                boolean saltar = tipo.equals("if") ? condicion : !condicion;

                if (saltar) {
                    pc = etiquetas.getOrDefault(destino, codigo.size());
                } else {
                    pc++;
                }
                continue;
            }

            Matcher mBin = PATRON_BINARIA.matcher(inst);
            if (mBin.matches()) {
                int v1 = obtenerValor(mBin.group(2), memoria);
                int v2 = obtenerValor(mBin.group(4), memoria);
                memoria.put(mBin.group(1), evaluarAritmetica(v1, mBin.group(3), v2));
                pc++;
                continue;
            }

            Matcher mAsig = PATRON_ASIGNACION.matcher(inst);
            if (mAsig.matches()) {
                memoria.put(mAsig.group(1), obtenerValor(mAsig.group(2), memoria));
                pc++;
                continue;
            }

            pc++;
        }

        return memoria;
    }

    private int obtenerValor(String token, Map<String, Integer> memoria) {
        if (token.matches("-?\\d+")) {
            return Integer.parseInt(token);
        }
        return memoria.getOrDefault(token, 0);
    }

    private int evaluarAritmetica(int a, String op, int b) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : 0;
            default -> 0;
        };
    }

    private boolean evaluarRelacional(int a, String op, int b) {
        return switch (op) {
            case "==" -> a == b;
            case "!=" -> a != b;
            case "<"  -> a < b;
            case "<=" -> a <= b;
            case ">"  -> a > b;
            case ">=" -> a >= b;
            default -> false;
        };
    }
}
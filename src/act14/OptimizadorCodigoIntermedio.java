/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act14;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptimizadorCodigoIntermedio {

    private final List<String> registroCambios = new ArrayList<>();

    private static final Pattern PATRON_ETIQUETA = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*):$");
    private static final Pattern PATRON_GOTO = Pattern.compile("^goto\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");
    private static final Pattern PATRON_IF = Pattern.compile(
            "^(if|ifFalse)\\s+([a-zA-Z0-9_]+)\\s*(==|!=|<=|>=|<|>)\\s*([a-zA-Z0-9_]+)\\s+goto\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");
    private static final Pattern PATRON_BINARIA = Pattern.compile(
            "^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*([a-zA-Z0-9_]+)\\s*([+\\-*/])\\s*([a-zA-Z0-9_]+)$");
    private static final Pattern PATRON_ASIGNACION = Pattern.compile(
            "^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*([a-zA-Z0-9_]+)$");
    private static final Pattern PATRON_SALIDA = Pattern.compile(
            "^(print|return|param)\\s+([a-zA-Z0-9_]+)$");
    private static final Pattern PATRON_ID = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");

    private static final Set<String> PALABRAS_RESERVADAS = Set.of(
            "goto", "if", "ifFalse", "print", "return", "param", "call"
    );

    /**
     * Aplica todas las reglas de optimización de forma iterativa hasta alcanzar punto fijo.
     */
    public List<String> optimizar(List<String> codigoOriginal, Set<String> variablesSalida) {
        registroCambios.clear();
        List<String> actual = new ArrayList<>();
        for (String linea : codigoOriginal) {
            String limpia = linea.trim();
            if (!limpia.isEmpty()) {
                actual.add(limpia);
            }
        }

        boolean huboCambios;
        int pasada = 0;
        int maxPasadas = 20;

        do {
            pasada++;
            int cambiosAntes = registroCambios.size();

            actual = eliminarCodigoInalcanzable(actual);
            actual = eliminarAsignacionesSobrescritas(actual);
            actual = eliminarCalculosRepetidos(actual);
            actual = simplificarYPropagar(actual);
            actual = eliminarVariablesNoUtilizadas(actual, variablesSalida);

            huboCambios = registroCambios.size() > cambiosAntes;
        } while (huboCambios && pasada < maxPasadas);

        return actual;
    }

    public List<String> getRegistroCambios() {
        return Collections.unmodifiableList(registroCambios);
    }

    // =========================================================================
    // REGLA 1A: Eliminación de Instrucciones Inalcanzables (Código Muerto)
    // =========================================================================
    private List<String> eliminarCodigoInalcanzable(List<String> codigo) {
        List<String> resultado = new ArrayList<>();
        boolean inalcanzable = false;

        for (String inst : codigo) {
            if (PATRON_ETIQUETA.matcher(inst).matches()) {
                inalcanzable = false;
                resultado.add(inst);
            } else if (inalcanzable) {
                registroCambios.add("[REGLA 1A - Código Inalcanzable] Eliminada: \"" + inst + "\"");
            } else {
                resultado.add(inst);
                if (PATRON_GOTO.matcher(inst).matches() || inst.startsWith("return")) {
                    inalcanzable = true;
                }
            }
        }
        return resultado;
    }

    // =========================================================================
    // REGLA 2B: Eliminación de Asignaciones Sobrescritas Inmediatamente
    // =========================================================================
    private List<String> eliminarAsignacionesSobrescritas(List<String> codigo) {
        List<String> resultado = new ArrayList<>();
        int n = codigo.size();

        for (int i = 0; i < n; i++) {
            String actual = codigo.get(i);
            String varActual = obtenerDestinoAsignacion(actual);

            if (varActual != null && i + 1 < n) {
                String siguiente = codigo.get(i + 1);
                String varSiguiente = obtenerDestinoAsignacion(siguiente);

                if (varActual.equals(varSiguiente) && !usaVariableEnDerecha(siguiente, varActual)) {
                    registroCambios.add("[REGLA 2B - Asignación Innecesaria] Eliminada: \"" + actual
                            + "\" (sobrescrita inmediatamente por \"" + siguiente + "\")");
                    continue;
                }
            }
            resultado.add(actual);
        }
        return resultado;
    }

    // =========================================================================
    // REGLA 2A: Eliminación de Cálculos Repetidos (CSE en bloque básico)
    // =========================================================================
    private List<String> eliminarCalculosRepetidos(List<String> codigo) {
        List<String> resultado = new ArrayList<>();
        Map<String, String> expresionesDisponibles = new LinkedHashMap<>();

        for (String inst : codigo) {
            if (esLimiteDeBloque(inst)) {
                expresionesDisponibles.clear();
                resultado.add(inst);
                continue;
            }

            Matcher mBin = PATRON_BINARIA.matcher(inst);
            if (mBin.matches()) {
                String destino = mBin.group(1);
                String op1 = mBin.group(2);
                String operador = mBin.group(3);
                String op2 = mBin.group(4);
                String expresion = normalizarExpresion(op1, operador, op2);

                if (expresionesDisponibles.containsKey(expresion)) {
                    String varPrevia = expresionesDisponibles.get(expresion);
                    String reemplazo = destino + " = " + varPrevia;
                    registroCambios.add("[REGLA 2A - Cálculo Repetido] \"" + inst
                            + "\" reemplazada por \"" + reemplazo + "\" (ya calculado en " + varPrevia + ")");
                    invalidarDependencias(expresionesDisponibles, destino);
                    resultado.add(reemplazo);
                } else {
                    invalidarDependencias(expresionesDisponibles, destino);
                    expresionesDisponibles.put(expresion, destino);
                    resultado.add(inst);
                }
                continue;
            }

            String destino = obtenerDestinoAsignacion(inst);
            if (destino != null) {
                invalidarDependencias(expresionesDisponibles, destino);
            }
            resultado.add(inst);
        }
        return resultado;
    }

    // =========================================================================
    // REGLA 3: Simplificación Algebraica, Plegado de Constantes y Copias
    // =========================================================================
    private List<String> simplificarYPropagar(List<String> codigo) {
        List<String> resultado = new ArrayList<>();
        Map<String, String> copias = new HashMap<>();
        Map<String, String[]> tempLineales = new HashMap<>();

        for (String inst : codigo) {
            if (esLimiteDeBloque(inst)) {
                copias.clear();
                tempLineales.clear();
                resultado.add(inst);
                continue;
            }

            Matcher mBin = PATRON_BINARIA.matcher(inst);
            if (mBin.matches()) {
                String dest = mBin.group(1);
                String op1 = mBin.group(2);
                String operador = mBin.group(3);
                String op2 = mBin.group(4);

                if (esEntero(op1) && esEntero(op2)) {
                    int val = operarEnteros(Integer.parseInt(op1), operador, Integer.parseInt(op2));
                    String nueva = dest + " = " + val;
                    registroCambios.add("[REGLA 3 - Plegado de Constantes] \"" + inst + "\" -> \"" + nueva + "\"");
                    limpiarReferencias(copias, tempLineales, dest);
                    resultado.add(nueva);
                    continue;
                }

                if ((operador.equals("+") || operador.equals("-")) && esEntero(op2) && tempLineales.containsKey(op1)) {
                    String[] prev = tempLineales.get(op1);
                    int c1 = prev[1].equals("+") ? Integer.parseInt(prev[2]) : -Integer.parseInt(prev[2]);
                    int c2 = operador.equals("+") ? Integer.parseInt(op2) : -Integer.parseInt(op2);
                    int total = c1 + c2;
                    String opFinal = total >= 0 ? "+" : "-";
                    String nueva = dest + " = " + prev[0] + " " + opFinal + " " + Math.abs(total);
                    registroCambios.add("[REGLA 3 - Simplificación Algebraica] \"" + inst + "\" -> \"" + nueva + "\"");
                    limpiarReferencias(copias, tempLineales, dest);
                    tempLineales.put(dest, new String[]{prev[0], opFinal, String.valueOf(Math.abs(total))});
                    resultado.add(nueva);
                    continue;
                }

                String op1Prop = copias.getOrDefault(op1, op1);
                String op2Prop = copias.getOrDefault(op2, op2);
                String nueva = dest + " = " + op1Prop + " " + operador + " " + op2Prop;
                if (!nueva.equals(inst)) {
                    registroCambios.add("[REGLA 2B - Propagación de Copia] \"" + inst + "\" -> \"" + nueva + "\"");
                }

                limpiarReferencias(copias, tempLineales, dest);
                if ((operador.equals("+") || operador.equals("-")) && !esEntero(op1Prop) && esEntero(op2Prop)) {
                    tempLineales.put(dest, new String[]{op1Prop, operador, op2Prop});
                }
                resultado.add(nueva);
                continue;
            }

            Matcher mAsig = PATRON_ASIGNACION.matcher(inst);
            if (mAsig.matches()) {
                String dest = mAsig.group(1);
                String fuente = mAsig.group(2);

                if (tempLineales.containsKey(fuente) && esTemporal(fuente)) {
                    String[] expr = tempLineales.get(fuente);
                    String nueva = dest + " = " + expr[0] + " " + expr[1] + " " + expr[2];
                    registroCambios.add("[REGLA 3 - Fusión de Temporal] \"" + inst + "\" -> \"" + nueva + "\"");
                    limpiarReferencias(copias, tempLineales, dest);
                    resultado.add(nueva);
                    continue;
                }

                if (copias.containsKey(fuente)) {
                    String origenReal = copias.get(fuente);
                    String nueva = dest + " = " + origenReal;
                    registroCambios.add("[REGLA 2B - Propagación de Copia] \"" + inst + "\" -> \"" + nueva + "\"");
                    limpiarReferencias(copias, tempLineales, dest);
                    if (!esEntero(origenReal)) {
                        copias.put(dest, origenReal);
                    }
                    resultado.add(nueva);
                    continue;
                }

                limpiarReferencias(copias, tempLineales, dest);
                if (!esEntero(fuente) && !dest.equals(fuente)) {
                    copias.put(dest, fuente);
                }
                resultado.add(inst);
                continue;
            }

            resultado.add(inst);
        }
        return resultado;
    }

    // =========================================================================
    // REGLA 1B: Eliminación de Variables No Utilizadas (Código Muerto)
    // =========================================================================
    private List<String> eliminarVariablesNoUtilizadas(List<String> codigo, Set<String> variablesSalida) {
        Set<String> variablesUsadas = recolectarVariablesUsadas(codigo);
        List<String> resultado = new ArrayList<>();

        for (String inst : codigo) {
            String destino = obtenerDestinoAsignacion(inst);
            if (destino != null && !variablesUsadas.contains(destino) && !variablesSalida.contains(destino)) {
                registroCambios.add("[REGLA 1B - Variable No Utilizada] Eliminada: \"" + inst
                        + "\" (la variable '" + destino + "' nunca se lee)");
                continue;
            }
            resultado.add(inst);
        }
        return resultado;
    }

    // =========================================================================
    // Métodos Auxiliares
    // =========================================================================
    private Set<String> recolectarVariablesUsadas(List<String> codigo) {
        Set<String> usadas = new HashSet<>();
        for (String inst : codigo) {
            Matcher mBin = PATRON_BINARIA.matcher(inst);
            if (mBin.matches()) {
                agregarSiEsIdentificador(usadas, mBin.group(2));
                agregarSiEsIdentificador(usadas, mBin.group(4));
                continue;
            }
            Matcher mAsig = PATRON_ASIGNACION.matcher(inst);
            if (mAsig.matches()) {
                agregarSiEsIdentificador(usadas, mAsig.group(2));
                continue;
            }
            Matcher mIf = PATRON_IF.matcher(inst);
            if (mIf.matches()) {
                agregarSiEsIdentificador(usadas, mIf.group(2));
                agregarSiEsIdentificador(usadas, mIf.group(4));
                continue;
            }
            Matcher mSal = PATRON_SALIDA.matcher(inst);
            if (mSal.matches()) {
                agregarSiEsIdentificador(usadas, mSal.group(2));
            }
        }
        return usadas;
    }

    private String obtenerDestinoAsignacion(String inst) {
        Matcher mBin = PATRON_BINARIA.matcher(inst);
        if (mBin.matches()) return mBin.group(1);
        Matcher mAsig = PATRON_ASIGNACION.matcher(inst);
        if (mAsig.matches()) return mAsig.group(1);
        return null;
    }

    private boolean usaVariableEnDerecha(String inst, String var) {
        int idx = inst.indexOf('=');
        if (idx == -1) return false;
        String derecha = inst.substring(idx + 1);
        Matcher m = PATRON_ID.matcher(derecha);
        while (m.find()) {
            if (m.group().equals(var)) return true;
        }
        return false;
    }

    private boolean esLimiteDeBloque(String inst) {
        return PATRON_ETIQUETA.matcher(inst).matches()
                || PATRON_GOTO.matcher(inst).matches()
                || PATRON_IF.matcher(inst).matches()
                || inst.startsWith("call ")
                || inst.startsWith("return");
    }

    private String normalizarExpresion(String op1, String operador, String op2) {
        if ((operador.equals("+") || operador.equals("*")) && op1.compareTo(op2) > 0) {
            return op2 + " " + operador + " " + op1;
        }
        return op1 + " " + operador + " " + op2;
    }

    private void invalidarDependencias(Map<String, String> exprMap, String varModificada) {
        exprMap.entrySet().removeIf(entry -> {
            String[] partes = entry.getKey().split(" ");
            return partes[0].equals(varModificada)
                    || partes[2].equals(varModificada)
                    || entry.getValue().equals(varModificada);
        });
    }

    private void limpiarReferencias(Map<String, String> copias, Map<String, String[]> tempLineales, String varModificada) {
        copias.remove(varModificada);
        copias.entrySet().removeIf(e -> e.getValue().equals(varModificada));
        tempLineales.remove(varModificada);
        tempLineales.entrySet().removeIf(e -> e.getValue()[0].equals(varModificada));
    }

    private void agregarSiEsIdentificador(Set<String> set, String token) {
        if (!esEntero(token) && !PALABRAS_RESERVADAS.contains(token)) {
            set.add(token);
        }
    }

    private boolean esEntero(String s) {
        return s.matches("-?\\d+");
    }

    private boolean esTemporal(String s) {
        return s.matches("t\\d+");
    }

    private int operarEnteros(int a, String op, int b) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : 0;
            default -> 0;
        };
    }
}
/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act12;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador semántico para el subconjunto de JavaScript (let, const, var,
 * asignaciones y expresiones). Valida ámbitos y tipos, y dirige la emisión
 * del código intermedio de tres direcciones.
 */
public class ControladorSemantico {
    private final TablaSimbolos tabla;
    private final GeneradorCodigoIntermedio gen;
    private final List<String> errores;

    public ControladorSemantico(List<String> codigoFuente) {
        this.tabla = new TablaSimbolos();
        this.gen = new GeneradorCodigoIntermedio(codigoFuente);
        this.errores = new ArrayList<>();
    }

    public TablaSimbolos getTabla() { return tabla; }
    public GeneradorCodigoIntermedio getGenerador() { return gen; }
    public List<String> getErrores() { return errores; }
    public boolean hayErrores() { return !errores.isEmpty(); }

    // ------------------------------------------------------------------
    // Registro de errores
    // ------------------------------------------------------------------

    private void registrar(String clase, int linea, int columna, String token, String causa) {
        String mensaje = String.format("Error %s en línea %d, columna %d [%s]: %s",
                clase, linea, columna, token, causa);
        errores.add(mensaje);
        gen.emitirError(mensaje);
    }

    public void errorSemantico(int linea, int columna, String token, String causa) {
        registrar("semántico", linea, columna, token, causa);
    }

    public void errorSintactico(int linea, int columna, String token, String causa) {
        registrar("sintáctico", linea, columna, token, causa);
    }

    public void errorLexico(int linea, int columna, String token) {
        registrar("léxico", linea, columna, token, "Carácter no reconocido.");
    }

    // ------------------------------------------------------------------
    // Ámbitos y sentencias
    // ------------------------------------------------------------------

    public void entrarBloque() {
        String nombre = tabla.entrarBloque();
        gen.marcarAmbito(">>> Entra al ámbito '" + nombre + "'");
    }

    public void salirBloque() {
        String nombre = tabla.obtenerAmbitoActual();
        tabla.salirAmbito();
        gen.marcarAmbito("<<< Sale del ámbito '" + nombre + "'");
    }

    public void finSentencia(int linea) {
        gen.cerrarSentencia(linea);
    }

    // ------------------------------------------------------------------
    // Declaraciones y asignaciones
    // ------------------------------------------------------------------

    /** let / const / var id [= inicial]; 'inicial' es null si no hay inicializador. */
    public void declarar(Simbolo.Clase clase, String id, NodoExpresion inicial, int linea, int columna) {
        boolean esVar = clase == Simbolo.Clase.VAR;
        Simbolo previo = esVar ? tabla.buscarGlobal(id) : tabla.buscarLocal(id);
        Simbolo simbolo;

        if (previo != null && esVar && previo.getClase() == Simbolo.Clase.VAR) {
            simbolo = previo; // JavaScript permite volver a declarar con 'var'
        } else if (previo != null) {
            errorSemantico(linea, columna, id, "El identificador '" + id + "' ya fue declarado en el ámbito '"
                    + previo.getAmbito() + "' (línea " + previo.getLinea() + ").");
            return;
        } else {
            String ambito = esVar ? tabla.obtenerAmbitoGlobal() : tabla.obtenerAmbitoActual();
            simbolo = new Simbolo(id, clase, TipoDato.UNDEFINED, ambito, linea, columna);
            if (esVar) {
                tabla.insertarGlobal(simbolo);
            } else {
                tabla.insertar(simbolo);
            }
        }

        if (inicial == null) {
            if (clase == Simbolo.Clase.CONST) {
                errorSemantico(linea, columna, id, "La constante '" + id + "' debe inicializarse al declararse.");
                simbolo.setTipo(TipoDato.ERROR);
            } else {
                gen.emitirComentario("Declaración de '" + id + "' sin valor inicial (undefined)");
            }
            return;
        }

        simbolo.setTipo(inicial.getTipo());
        if (clase == Simbolo.Clase.CONST && inicial.esConstante()) {
            simbolo.setValorConstante(inicial.getValor());
        }
        gen.emitirAsignacion(id, inicial);
    }

    /** id = valor; */
    public void asignar(String id, NodoExpresion valor, int linea, int columna) {
        Simbolo simbolo = tabla.buscar(id);
        if (simbolo == null) {
            errorSemantico(linea, columna, id, "La variable '" + id + "' no ha sido declarada en un ámbito visible.");
            return;
        }
        if (simbolo.esConstante()) {
            errorSemantico(linea, columna, id, "No se puede reasignar la constante '" + id + "'.");
            return;
        }

        TipoDato tipoVar = simbolo.getTipo();
        TipoDato tipoValor = valor.getTipo();
        if (tipoValor != TipoDato.ERROR) {
            if (tipoVar == TipoDato.UNDEFINED || tipoVar == TipoDato.ERROR) {
                simbolo.setTipo(tipoValor);
            } else if (tipoVar != tipoValor) {
                gen.emitirValidacionMismoTipo(id, valor.getLugar());
                errorSemantico(linea, columna, id, "Type mismatch: '" + id + "' es de tipo '" + tipoVar
                        + "' y se le asigna un valor de tipo '" + tipoValor + "'.");
            }
        }
        gen.emitirAsignacion(id, valor);
    }

    // ------------------------------------------------------------------
    // Expresiones
    // ------------------------------------------------------------------

    public NodoExpresion literal(TipoDato tipo, String lexema) {
        return NodoExpresion.constante(tipo, lexema);
    }

    public NodoExpresion usoVariable(String id, int linea, int columna) {
        Simbolo simbolo = tabla.buscar(id);
        if (simbolo == null) {
            errorSemantico(linea, columna, id, "La variable '" + id + "' no ha sido declarada en un ámbito visible.");
            return NodoExpresion.variable(TipoDato.ERROR, id, null);
        }
        return NodoExpresion.variable(simbolo.getTipo(), id, simbolo.getValorConstante());
    }

    public NodoExpresion unaria(String op, NodoExpresion a, int linea, int columna) {
        a = gen.materializar(a);
        TipoDato esperado = op.equals("-") ? TipoDato.NUMBER : TipoDato.BOOLEAN;

        // Un literal numérico negado sigue siendo una constante: -5
        if (op.equals("-") && a.esConstante() && a.getTipo() == TipoDato.NUMBER) {
            return NodoExpresion.constante(TipoDato.NUMBER, "-" + a.getLugar());
        }

        boolean bloqueada = false;
        if (a.getTipo() != TipoDato.ERROR && a.getTipo() != esperado) {
            gen.emitirValidacionTipo(a, esperado);
            errorSemantico(linea, columna, op, "Type mismatch: el operador '" + op + "' requiere un operando '"
                    + esperado + "' y se encontró '" + a.getTipo() + "'.");
            bloqueada = true;
        }
        TipoDato resultado = (bloqueada || a.getTipo() == TipoDato.ERROR) ? TipoDato.ERROR : esperado;
        return gen.emitirUnaria(op, a, resultado, bloqueada);
    }

    public NodoExpresion binaria(String op, NodoExpresion a, NodoExpresion b, int linea, int columna) {
        a = gen.materializar(a);
        b = gen.materializar(b);
        TipoDato ta = a.getTipo();
        TipoDato tb = b.getTipo();
        boolean errorPrevio = ta == TipoDato.ERROR || tb == TipoDato.ERROR;
        boolean ambosNumber = ta == TipoDato.NUMBER && tb == TipoDato.NUMBER;
        boolean ambosString = ta == TipoDato.STRING && tb == TipoDato.STRING;

        boolean esLogica = op.equals("&&") || op.equals("||");
        boolean esIgualdad = op.equals("==") || op.equals("!=") || op.equals("===") || op.equals("!==");
        boolean esRelacional = op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=");
        boolean esDivision = op.equals("/") || op.equals("%");

        boolean tiposValidos;
        TipoDato resultado;
        TipoDato esperado; // null => basta con que ambos operandos sean del mismo tipo
        if (esLogica) {
            tiposValidos = ta == TipoDato.BOOLEAN && tb == TipoDato.BOOLEAN;
            resultado = TipoDato.BOOLEAN;
            esperado = TipoDato.BOOLEAN;
        } else if (esIgualdad) {
            tiposValidos = ta == tb && ta != TipoDato.UNDEFINED;
            resultado = TipoDato.BOOLEAN;
            esperado = null;
        } else if (esRelacional) {
            tiposValidos = ambosNumber || ambosString;
            resultado = TipoDato.BOOLEAN;
            esperado = TipoDato.NUMBER;
        } else if (op.equals("+")) {
            tiposValidos = ambosNumber || ambosString; // suma o concatenación
            resultado = ambosString ? TipoDato.STRING : TipoDato.NUMBER;
            esperado = TipoDato.NUMBER;
        } else {
            tiposValidos = ambosNumber;
            resultado = TipoDato.NUMBER;
            esperado = TipoDato.NUMBER;
        }

        boolean bloqueada = false;
        if (!errorPrevio && !tiposValidos) {
            if (esperado == null) {
                gen.emitirValidacionMismoTipo(a.getLugar(), b.getLugar());
            } else {
                gen.emitirValidacionTipos(a, b, esperado);
            }
            errorSemantico(linea, columna, op, "Type mismatch: el operador '" + op
                    + "' no puede aplicarse a operandos de tipo '" + ta + "' y '" + tb + "'.");
            bloqueada = true;
        }

        if (esDivision) {
            if (b.esCeroConocido()) {
                // El divisor es 0 (literal o 'const'): el error se conoce desde la compilación
                gen.emitirValidacionDivision(b);
                errorSemantico(linea, columna, op, "Division by zero: el divisor '" + b.getLugar()
                        + "' siempre vale 0.");
                bloqueada = true;
            } else if (b.getValor() == null && (tb == TipoDato.NUMBER || tb == TipoDato.ERROR)) {
                // Divisor variable o temporal: la validación queda para tiempo de ejecución
                gen.emitirValidacionDivision(b);
            }
        }

        if (errorPrevio || bloqueada) {
            resultado = TipoDato.ERROR;
        }
        if (esLogica && !bloqueada) {
            return gen.operacionPendiente(op, a, b, resultado);
        }
        return gen.emitirBinaria(op, a, b, resultado, bloqueada);
    }
}

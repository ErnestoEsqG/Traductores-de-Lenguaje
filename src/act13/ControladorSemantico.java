/*
 * Camarena Velazquez Alberto Dario
 * Esqueda Guzman Ernesto
 * Nava Gutierrez Alan Santiago
 */
package act13;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Controlador semántico para el subconjunto de JavaScript. Valida ámbitos y
 * tipos, y dirige la emisión del código intermedio de tres direcciones para
 * expresiones, estructuras de control, funciones y clases.
 */
public class ControladorSemantico {

    /** Datos de un 'if' entre su encabezado y su cierre. */
    public static class InfoIf {
        String etiquetaIf;
        String etiquetaFin;
        String condicion;
        int posicion;
    }

    /** Estructura que acepta 'break' / 'continue', o frontera de una función. */
    private static class Contexto {
        enum Tipo { BUCLE, SWITCH, FUNCION }

        final Tipo tipo;
        String etiquetaInicio;     // inicio del bucle
        String etiquetaFin;        // destino de 'break'; null si aún no se conoce
        String etiquetaContinue;   // destino de 'continue'; null si aún no se conoce
        final List<GeneradorCodigoIntermedio.Linea> breaksPendientes = new ArrayList<>();
        final List<GeneradorCodigoIntermedio.Linea> continuesPendientes = new ArrayList<>();
        // for
        int posicion;
        List<GeneradorCodigoIntermedio.Linea> actualizacion = new ArrayList<>();
        // switch
        String temporal;
        TipoDato tipoSwitch;
        String etiquetaDefault;
        final List<GeneradorCodigoIntermedio.Linea> pruebas = new ArrayList<>();

        Contexto(Tipo tipo) {
            this.tipo = tipo;
        }
    }

    /** Llamada a una función o método que se define más adelante en el código fuente. */
    private static class LlamadaPendiente {
        GeneradorCodigoIntermedio.Linea instruccion;
        String nombre;
        String descripcion;
        int argumentos;
        int linea;
        int columna;
    }

    /** Marca que el objeto es de tipo desconocido (parámetro): sus miembros no se validan. */
    private static final Simbolo CLASE_DINAMICA =
            new Simbolo("?", Simbolo.Clase.CLASE, TipoDato.ANY, "-", 0, 0);

    private final TablaSimbolos tabla;
    private final GeneradorCodigoIntermedio gen;
    private final List<String> errores = new ArrayList<>();
    private final Stack<Contexto> contextos = new Stack<>();
    private final Stack<Simbolo> rutinas = new Stack<>();
    private final List<LlamadaPendiente> funcionesPendientes = new ArrayList<>();
    private final List<LlamadaPendiente> metodosPendientes = new ArrayList<>();
    private Simbolo claseActual;

    public ControladorSemantico(List<String> codigoFuente) {
        this.tabla = new TablaSimbolos();
        this.gen = new GeneradorCodigoIntermedio(codigoFuente);
    }

    public TablaSimbolos getTabla() { return tabla; }
    public GeneradorCodigoIntermedio getGenerador() { return gen; }
    public List<String> getErrores() { return errores; }

    // ==================================================================
    // Registro de errores
    // ==================================================================

    private void registrar(String clase, int linea, int columna, String token, String causa) {
        String mensaje = String.format("Error %s en línea %d, columna %d [%s]: %s",
                clase, linea, columna, token, causa);
        errores.add(mensaje);
        gen.marcarLinea(linea);
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

    // ==================================================================
    // Ámbitos
    // ==================================================================

    public void entrarBloque() {
        tabla.entrarBloque();
    }

    public void salirBloque() {
        tabla.salirAmbito();
    }

    public int posicion() {
        return gen.posicion();
    }

    // ==================================================================
    // Declaraciones y asignaciones
    // ==================================================================

    /** let / const / var id [= inicial]; 'inicial' es null si no hay inicializador. */
    public void declarar(Simbolo.Clase clase, String id, NodoExpresion inicial, int linea, int columna) {
        gen.marcarLinea(linea);
        boolean esVar = clase == Simbolo.Clase.VAR;
        Simbolo previo = esVar ? tabla.buscarEnAmbitoFuncion(id) : tabla.buscarLocal(id);
        Simbolo simbolo;

        if (previo != null && esVar && previo.getClase() == Simbolo.Clase.VAR) {
            simbolo = previo; // JavaScript permite volver a declarar con 'var'
        } else if (previo != null) {
            errorSemantico(linea, columna, id, "El identificador '" + id + "' ya fue declarado en el ámbito '"
                    + previo.getAmbito() + "' (línea " + previo.getLinea() + ").");
            return;
        } else {
            String ambito = esVar ? tabla.obtenerAmbitoFuncion() : tabla.obtenerAmbitoActual();
            simbolo = new Simbolo(id, clase, TipoDato.UNDEFINED, ambito, linea, columna);
            if (esVar) {
                tabla.insertarEnAmbitoFuncion(simbolo);
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
        simbolo.setNombreClase(inicial.getClase());
        if (clase == Simbolo.Clase.CONST && inicial.esConstante()) {
            simbolo.setValorConstante(inicial.getValor());
        }
        emitirAsignacion(id, inicial);
    }

    /** Busca una variable que pueda recibir un valor; reporta el error y devuelve null si no se puede. */
    private Simbolo variableAsignable(String id, int linea, int columna) {
        Simbolo simbolo = tabla.buscar(id);
        if (simbolo == null) {
            errorSemantico(linea, columna, id, "La variable '" + id + "' no ha sido declarada en un ámbito visible.");
            return null;
        }
        if (simbolo.esConstante()) {
            errorSemantico(linea, columna, id, "No se puede reasignar la constante '" + id + "'.");
            return null;
        }
        if (!simbolo.esVariable()) {
            errorSemantico(linea, columna, id, "'" + id + "' es " + articulo(simbolo) + " y no puede recibir un valor.");
            return null;
        }
        return simbolo;
    }

    private String articulo(Simbolo simbolo) {
        if (simbolo.esVariable()) {
            return "una variable";
        }
        return (simbolo.getClase() == Simbolo.Clase.METODO || simbolo.getClase() == Simbolo.Clase.ATRIBUTO
                ? "un " : "una ") + simbolo.getClase();
    }

    /** Emite la asignación, salvo que el valor provenga de una expresión que no pudo resolverse. */
    private void emitirAsignacion(String destino, NodoExpresion valor) {
        if (valor.getTipo() == TipoDato.ERROR && !valor.esTemporal()) {
            gen.emitirComentario("Asignación a '" + destino + "' omitida por el error anterior");
        } else {
            gen.emitirAsignacion(destino, valor);
        }
    }

    /** Actualiza o valida el tipo del destino de una asignación. */
    private void validarAsignacion(Simbolo destino, String lugarDestino, NodoExpresion valor, int linea, int columna) {
        TipoDato tipoDestino = destino.getTipo();
        TipoDato tipoValor = valor.getTipo();
        if (tipoValor == TipoDato.ERROR) {
            return;
        }
        if (tipoDestino == TipoDato.UNDEFINED || tipoDestino == TipoDato.ERROR) {
            destino.setTipo(tipoValor);
            destino.setNombreClase(valor.getClase());
        } else if (tipoDestino != TipoDato.ANY && tipoValor != TipoDato.ANY && tipoDestino != tipoValor) {
            gen.emitirValidacionMismoTipo(lugarDestino, valor.getLugar());
            errorSemantico(linea, columna, lugarDestino, "Type mismatch: '" + lugarDestino + "' es de tipo '"
                    + tipoDestino + "' y se le asigna un valor de tipo '" + tipoValor + "'.");
        }
    }

    /** id = valor */
    public void asignar(String id, NodoExpresion valor, int linea, int columna) {
        gen.marcarLinea(linea);
        Simbolo simbolo = variableAsignable(id, linea, columna);
        if (simbolo == null) {
            return;
        }
        validarAsignacion(simbolo, id, valor, linea, columna);
        emitirAsignacion(id, valor);
    }

    /** id += valor (también -=, *=, /=) */
    public void asignarCompuesta(String id, String operador, NodoExpresion valor, int linea, int columna) {
        String op = operador.substring(0, 1);
        NodoExpresion resultado = binaria(op, usoVariable(id, linea, columna), valor, linea, columna);
        if (tabla.buscar(id) != null) {
            asignar(id, resultado, linea, columna);
        }
    }

    /** id++ / id-- : se emite "id = id + 1" directamente. */
    public void incremento(String id, String op, int linea, int columna) {
        gen.marcarLinea(linea);
        Simbolo simbolo = variableAsignable(id, linea, columna);
        if (simbolo == null) {
            return;
        }
        if (!es(simbolo.getTipo(), TipoDato.NUMBER)) {
            gen.emitirValidacionTipo(id, TipoDato.NUMBER);
            errorSemantico(linea, columna, id, "Type mismatch: '" + op + op + "' requiere una variable 'number' y '"
                    + id + "' es '" + simbolo.getTipo() + "'.");
        }
        gen.emitir(id + " = " + id + " " + op + " 1", (op.equals("+") ? "Incrementa " : "Decrementa ") + id);
    }

    // ==================================================================
    // Expresiones
    // ==================================================================

    private boolean es(TipoDato tipo, TipoDato esperado) {
        return tipo == esperado || tipo == TipoDato.ANY;
    }

    public NodoExpresion literal(TipoDato tipo, String lexema) {
        return NodoExpresion.constante(tipo, lexema);
    }

    public NodoExpresion usoVariable(String id, int linea, int columna) {
        Simbolo simbolo = tabla.buscar(id);
        if (simbolo == null) {
            errorSemantico(linea, columna, id, "La variable '" + id + "' no ha sido declarada en un ámbito visible.");
            return NodoExpresion.variable(TipoDato.ERROR, id, null, null);
        }
        if (!simbolo.esVariable()) {
            errorSemantico(linea, columna, id, "'" + id + "' es " + articulo(simbolo)
                    + " y no puede usarse como variable.");
            return NodoExpresion.variable(TipoDato.ERROR, id, null, null);
        }
        return NodoExpresion.variable(simbolo.getTipo(), id, simbolo.getValorConstante(), simbolo.getNombreClase());
    }

    public NodoExpresion unaria(String op, NodoExpresion a, int linea, int columna) {
        gen.marcarLinea(linea);
        a = gen.materializar(a);
        TipoDato esperado = op.equals("-") ? TipoDato.NUMBER : TipoDato.BOOLEAN;

        // Un literal numérico negado sigue siendo una constante: -5
        if (op.equals("-") && a.esConstante() && a.getTipo() == TipoDato.NUMBER) {
            return NodoExpresion.constante(TipoDato.NUMBER, "-" + a.getLugar());
        }

        boolean bloqueada = false;
        if (a.getTipo() != TipoDato.ERROR && !es(a.getTipo(), esperado)) {
            gen.emitirValidacionTipo(a.getLugar(), esperado);
            errorSemantico(linea, columna, op, "Type mismatch: el operador '" + op + "' requiere un operando '"
                    + esperado + "' y se encontró '" + a.getTipo() + "'.");
            bloqueada = true;
        }
        TipoDato resultado = (bloqueada || a.getTipo() == TipoDato.ERROR) ? TipoDato.ERROR : esperado;
        return gen.emitirUnaria(op, a, resultado, bloqueada);
    }

    public NodoExpresion binaria(String op, NodoExpresion a, NodoExpresion b, int linea, int columna) {
        gen.marcarLinea(linea);
        a = gen.materializar(a);
        b = gen.materializar(b);
        TipoDato ta = a.getTipo();
        TipoDato tb = b.getTipo();
        boolean errorPrevio = ta == TipoDato.ERROR || tb == TipoDato.ERROR;
        boolean ambosNumber = es(ta, TipoDato.NUMBER) && es(tb, TipoDato.NUMBER);
        boolean ambosString = es(ta, TipoDato.STRING) && es(tb, TipoDato.STRING);

        boolean esLogica = op.equals("&&") || op.equals("||");
        boolean esIgualdad = op.equals("==") || op.equals("!=") || op.equals("===") || op.equals("!==");
        boolean esRelacional = op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=");
        boolean esDivision = op.equals("/") || op.equals("%");

        boolean tiposValidos;
        TipoDato resultado;
        TipoDato esperado; // null => basta con que ambos operandos sean del mismo tipo
        if (esLogica) {
            tiposValidos = es(ta, TipoDato.BOOLEAN) && es(tb, TipoDato.BOOLEAN);
            resultado = TipoDato.BOOLEAN;
            esperado = TipoDato.BOOLEAN;
        } else if (esIgualdad) {
            tiposValidos = (ta == tb || ta == TipoDato.ANY || tb == TipoDato.ANY)
                    && ta != TipoDato.UNDEFINED && tb != TipoDato.UNDEFINED;
            resultado = TipoDato.BOOLEAN;
            esperado = null;
        } else if (esRelacional) {
            tiposValidos = ambosNumber || ambosString;
            resultado = TipoDato.BOOLEAN;
            esperado = TipoDato.NUMBER;
        } else if (op.equals("+")) {
            tiposValidos = ambosNumber || ambosString; // suma o concatenación
            if (ta == TipoDato.STRING || tb == TipoDato.STRING) {
                resultado = TipoDato.STRING;
            } else if (ta == TipoDato.ANY || tb == TipoDato.ANY) {
                resultado = TipoDato.ANY;
            } else {
                resultado = TipoDato.NUMBER;
            }
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
            } else if (b.getValor() == null && (es(tb, TipoDato.NUMBER) || tb == TipoDato.ERROR)) {
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

    /** Evalúa la condición de una estructura de control y devuelve el lugar donde queda su valor. */
    private String condicion(NodoExpresion cond, String estructura, int linea, int columna) {
        gen.marcarLinea(linea);
        cond = gen.materializar(cond);
        TipoDato tipo = cond.getTipo();
        if (tipo != TipoDato.ERROR && !es(tipo, TipoDato.BOOLEAN)) {
            gen.emitirValidacionTipo(cond.getLugar(), TipoDato.BOOLEAN);
            errorSemantico(linea, columna, estructura, "Type mismatch: la condición de '" + estructura
                    + "' debe ser 'boolean' y se encontró '" + tipo + "'.");
        }
        return cond.getLugar();
    }

    // ==================================================================
    // if / else
    // ==================================================================

    /** if (cond): emite "if t goto L_if"; el resto se acomoda al cerrar la estructura. */
    public InfoIf inicioIf(NodoExpresion cond, int linea, int columna) {
        InfoIf info = new InfoIf();
        info.condicion = condicion(cond, "if", linea, columna);
        info.etiquetaIf = gen.nuevaEtiqueta();
        info.etiquetaFin = gen.nuevaEtiqueta();
        gen.emitir("if " + info.condicion + " goto " + info.etiquetaIf,
                "Si " + info.condicion + " es verdadero, salta a la etiqueta " + info.etiquetaIf);
        info.posicion = gen.posicion();
        return info;
    }

    /**
     * Cierra el if. El analizador genera primero el bloque if y después el else;
     * aquí se reacomodan para obtener: [else] goto L_fin, L_if: [if] L_fin:
     */
    public void finIf(InfoIf info, int posicionElse, boolean tieneElse) {
        List<GeneradorCodigoIntermedio.Linea> bloqueIf = gen.extraer(info.posicion, posicionElse);
        gen.emitir("goto " + info.etiquetaFin, tieneElse
                ? "Salta a la etiqueta " + info.etiquetaFin + " después del bloque else"
                : "Si " + info.condicion + " es falso, salta a la etiqueta " + info.etiquetaFin);
        gen.emitirEtiqueta(info.etiquetaIf, "Etiqueta " + info.etiquetaIf + " para el bloque if");
        gen.agregar(bloqueIf);
        gen.emitirEtiqueta(info.etiquetaFin,
                "Etiqueta " + info.etiquetaFin + " marca el final de la estructura condicional");
    }

    // ==================================================================
    // while
    // ==================================================================

    public void inicioWhile(int linea) {
        gen.marcarLinea(linea);
        Contexto ctx = new Contexto(Contexto.Tipo.BUCLE);
        ctx.etiquetaInicio = gen.nuevaEtiqueta();
        ctx.etiquetaFin = gen.nuevaEtiqueta();
        ctx.etiquetaContinue = ctx.etiquetaInicio;
        contextos.push(ctx);
        gen.emitirEtiqueta(ctx.etiquetaInicio, "Etiqueta " + ctx.etiquetaInicio + " marca el inicio del bucle");
    }

    public void condicionWhile(NodoExpresion cond, int linea, int columna) {
        Contexto ctx = contextos.peek();
        String c = condicion(cond, "while", linea, columna);
        gen.emitir("if not " + c + " goto " + ctx.etiquetaFin,
                "Si " + c + " es falso, salta a la etiqueta " + ctx.etiquetaFin + " (fin del bucle)");
    }

    public void finWhile() {
        Contexto ctx = contextos.pop();
        gen.emitir("goto " + ctx.etiquetaInicio, "Salta de vuelta al inicio del bucle");
        gen.emitirEtiqueta(ctx.etiquetaFin, "Etiqueta " + ctx.etiquetaFin + " marca el final del bucle");
    }

    // ==================================================================
    // do-while
    // ==================================================================

    public void inicioDo(int linea) {
        gen.marcarLinea(linea);
        Contexto ctx = new Contexto(Contexto.Tipo.BUCLE);
        ctx.etiquetaInicio = gen.nuevaEtiqueta();
        contextos.push(ctx);
        gen.emitirEtiqueta(ctx.etiquetaInicio, "Etiqueta " + ctx.etiquetaInicio + " marca el inicio del bucle");
    }

    /** Antes de la condición: aquí llegan los 'continue' del cuerpo, si los hubo. */
    public void condicionDo() {
        Contexto ctx = contextos.peek();
        if (!ctx.continuesPendientes.isEmpty()) {
            String etiqueta = gen.nuevaEtiqueta();
            gen.emitirEtiqueta(etiqueta, "Etiqueta " + etiqueta + " marca la condición del bucle (destino de continue)");
            resolverContinues(ctx, etiqueta);
        }
    }

    public void finDo(NodoExpresion cond, int linea, int columna) {
        Contexto ctx = contextos.pop();
        String c = condicion(cond, "do-while", linea, columna);
        gen.emitir("if " + c + " goto " + ctx.etiquetaInicio,
                "Si " + c + " es verdadero, salta de vuelta al inicio del bucle");
        if (!ctx.breaksPendientes.isEmpty()) {
            String etiqueta = gen.nuevaEtiqueta();
            resolverBreaks(ctx, etiqueta, ", fin del bucle");
            gen.emitirEtiqueta(etiqueta, "Etiqueta " + etiqueta + " marca el final del bucle");
        }
    }

    // ==================================================================
    // for
    // ==================================================================

    /** for ( : abre el ámbito de la variable de control. */
    public void inicioFor(int linea) {
        gen.marcarLinea(linea);
        tabla.entrarBloque();
        contextos.push(new Contexto(Contexto.Tipo.BUCLE));
    }

    /** Después de la inicialización: etiqueta de inicio del bucle. */
    public void etiquetaFor() {
        Contexto ctx = contextos.peek();
        ctx.etiquetaInicio = gen.nuevaEtiqueta();
        ctx.etiquetaFin = gen.nuevaEtiqueta();
        gen.emitirEtiqueta(ctx.etiquetaInicio, "Etiqueta " + ctx.etiquetaInicio + " marca el inicio del bucle");
    }

    /** Condición de entrada; cond es null en "for (;;)". */
    public void condicionFor(NodoExpresion cond, int linea, int columna) {
        Contexto ctx = contextos.peek();
        if (cond != null) {
            String c = condicion(cond, "for", linea, columna);
            gen.emitir("if not " + c + " goto " + ctx.etiquetaFin,
                    "Si " + c + " es falso, salta a la etiqueta " + ctx.etiquetaFin + " (fin del bucle)");
        }
        ctx.posicion = gen.posicion();
    }

    /** El incremento aparece antes del cuerpo en el fuente: se retira para emitirlo después. */
    public void actualizacionFor() {
        Contexto ctx = contextos.peek();
        ctx.actualizacion = gen.extraer(ctx.posicion, gen.posicion());
    }

    public void finFor() {
        Contexto ctx = contextos.pop();
        if (!ctx.continuesPendientes.isEmpty()) {
            String etiqueta = gen.nuevaEtiqueta();
            gen.emitirEtiqueta(etiqueta, "Etiqueta " + etiqueta + " marca el incremento del bucle (destino de continue)");
            resolverContinues(ctx, etiqueta);
        }
        gen.agregar(ctx.actualizacion);
        gen.emitir("goto " + ctx.etiquetaInicio, "Salta de vuelta al inicio del bucle");
        gen.emitirEtiqueta(ctx.etiquetaFin, "Etiqueta " + ctx.etiquetaFin + " marca el final del bucle");
        tabla.salirAmbito();
    }

    // ==================================================================
    // switch
    // ==================================================================

    public void inicioSwitch(NodoExpresion expresion, int linea, int columna) {
        gen.marcarLinea(linea);
        expresion = gen.materializar(expresion);
        Contexto ctx = new Contexto(Contexto.Tipo.SWITCH);
        ctx.temporal = gen.nuevoTemporal();
        ctx.tipoSwitch = expresion.getTipo();
        gen.emitir(ctx.temporal + " = " + expresion.getLugar(),
                ctx.temporal + " es una etiqueta temporal para el valor de " + expresion.getLugar());
        ctx.posicion = gen.posicion(); // aquí se insertarán las comparaciones de los casos
        contextos.push(ctx);
        tabla.entrarBloque();
    }

    public void caso(NodoExpresion valor, int linea, int columna) {
        Contexto ctx = contextos.peek();
        TipoDato t = ctx.tipoSwitch;
        if (t != TipoDato.ERROR && t != TipoDato.ANY && t != valor.getTipo()) {
            errorSemantico(linea, columna, valor.getLugar(), "Type mismatch: el caso " + valor.getLugar()
                    + " es '" + valor.getTipo() + "' y la expresión del switch es '" + t + "'.");
        }
        String etiqueta = gen.nuevaEtiqueta();
        ctx.pruebas.add(gen.crearInstruccion("if " + ctx.temporal + " == " + valor.getLugar() + " goto " + etiqueta,
                "Si " + ctx.temporal + " es " + valor.getLugar() + ", salta a la etiqueta " + etiqueta));
        gen.marcarLinea(linea);
        gen.emitirEtiqueta(etiqueta, "Etiqueta " + etiqueta + " para el caso " + valor.getLugar());
    }

    public void casoDefault(int linea, int columna) {
        Contexto ctx = contextos.peek();
        if (ctx.etiquetaDefault != null) {
            errorSemantico(linea, columna, "default", "El switch ya tiene un caso 'default'.");
            return;
        }
        ctx.etiquetaDefault = gen.nuevaEtiqueta();
        gen.marcarLinea(linea);
        gen.emitirEtiqueta(ctx.etiquetaDefault, "Etiqueta " + ctx.etiquetaDefault + " para el caso default");
    }

    public void finSwitch() {
        Contexto ctx = contextos.pop();
        String etiquetaFin = gen.nuevaEtiqueta();
        String destino = ctx.etiquetaDefault != null ? ctx.etiquetaDefault : etiquetaFin;
        ctx.pruebas.add(gen.crearInstruccion("goto " + destino,
                "Si " + ctx.temporal + " no coincide con ningún caso, salta a la etiqueta " + destino));
        gen.insertar(ctx.posicion, ctx.pruebas);
        resolverBreaks(ctx, etiquetaFin, " después de manejar el caso");
        gen.emitirEtiqueta(etiquetaFin, "Etiqueta " + etiquetaFin + " marca el final de la estructura switch");
        tabla.salirAmbito();
    }

    // ==================================================================
    // break / continue
    // ==================================================================

    private void resolverBreaks(Contexto ctx, String etiqueta, String motivo) {
        for (GeneradorCodigoIntermedio.Linea salto : ctx.breaksPendientes) {
            gen.parchar(salto, "goto " + etiqueta, "Salta a la etiqueta " + etiqueta + motivo + " (break)");
        }
        ctx.breaksPendientes.clear();
    }

    private void resolverContinues(Contexto ctx, String etiqueta) {
        for (GeneradorCodigoIntermedio.Linea salto : ctx.continuesPendientes) {
            gen.parchar(salto, "goto " + etiqueta, "Salta a la etiqueta " + etiqueta + " para la siguiente iteración (continue)");
        }
        ctx.continuesPendientes.clear();
    }

    /** Estructura más cercana que acepta el salto, sin cruzar la frontera de una función. */
    private Contexto contextoDeSalto(boolean soloBucles) {
        for (int i = contextos.size() - 1; i >= 0; i--) {
            Contexto ctx = contextos.get(i);
            if (ctx.tipo == Contexto.Tipo.FUNCION) {
                return null;
            }
            if (!soloBucles || ctx.tipo == Contexto.Tipo.BUCLE) {
                return ctx;
            }
        }
        return null;
    }

    public void sentenciaBreak(int linea, int columna) {
        gen.marcarLinea(linea);
        Contexto ctx = contextoDeSalto(false);
        if (ctx == null) {
            errorSemantico(linea, columna, "break", "'break' solo puede usarse dentro de un bucle o de un switch.");
        } else if (ctx.etiquetaFin != null) {
            gen.emitir("goto " + ctx.etiquetaFin, "Salta a la etiqueta " + ctx.etiquetaFin + ", fin del bucle (break)");
        } else {
            ctx.breaksPendientes.add(gen.emitir("goto ?", "break"));
        }
    }

    public void sentenciaContinue(int linea, int columna) {
        gen.marcarLinea(linea);
        Contexto ctx = contextoDeSalto(true);
        if (ctx == null) {
            errorSemantico(linea, columna, "continue", "'continue' solo puede usarse dentro de un bucle.");
        } else if (ctx.etiquetaContinue != null) {
            gen.emitir("goto " + ctx.etiquetaContinue,
                    "Salta a la etiqueta " + ctx.etiquetaContinue + " para la siguiente iteración (continue)");
        } else {
            ctx.continuesPendientes.add(gen.emitir("goto ?", "continue"));
        }
    }

    // ==================================================================
    // Funciones y métodos
    // ==================================================================

    public void inicioFuncion(String id, List<String> parametros, int linea, int columna) {
        gen.marcarLinea(linea);
        Simbolo funcion = new Simbolo(id, Simbolo.Clase.FUNCION, TipoDato.ANY, tabla.obtenerAmbitoActual(), linea, columna);
        if (!tabla.insertar(funcion)) {
            errorSemantico(linea, columna, id, "El identificador '" + id + "' ya fue declarado en este ámbito.");
        }
        inicioRutina(funcion, parametros, "la función " + id, id, linea, columna);
    }

    private void inicioRutina(Simbolo rutina, List<String> parametros, String descripcion, String nombreAmbito,
                              int linea, int columna) {
        rutina.setEtiqueta(gen.nuevaEtiqueta());
        rutina.setParametros(parametros);
        gen.emitirComentario("Definición de " + descripcion + " (parámetros: "
                + (parametros.isEmpty() ? "ninguno" : String.join(", ", parametros)) + ")");
        gen.emitirEtiqueta(rutina.getEtiqueta(),
                "Etiqueta " + rutina.getEtiqueta() + " marca el inicio de " + descripcion);

        tabla.entrarAmbito(nombreAmbito, true);
        for (String p : parametros) {
            // Los parámetros de JavaScript no declaran tipo: se tratan como 'any'
            if (!tabla.insertar(new Simbolo(p, Simbolo.Clase.PARAMETRO, TipoDato.ANY, nombreAmbito, linea, columna))) {
                errorSemantico(linea, columna, p, "El parámetro '" + p + "' está repetido.");
            }
        }
        rutinas.push(rutina);
        contextos.push(new Contexto(Contexto.Tipo.FUNCION));
    }

    public void finRutina() {
        Simbolo rutina = rutinas.pop();
        contextos.pop();
        tabla.salirAmbito();
        String descripcion = rutina.getClase() == Simbolo.Clase.FUNCION ? "la función " + rutina.getNombre()
                : rutina.getNombre().equals("constructor") ? "el constructor" : "el método " + rutina.getNombre();
        if (!gen.ultimaEsReturn()) {
            gen.emitir("return", "Fin de " + descripcion + ": regresa al punto de llamada");
        }
        if (!rutina.retornaValor()) {
            rutina.setTipo(TipoDato.UNDEFINED); // no retorna valor
        }
        gen.emitirComentario("Fin de " + descripcion);
    }

    public void sentenciaReturn(NodoExpresion valor, int linea, int columna) {
        gen.marcarLinea(linea);
        if (rutinas.isEmpty()) {
            errorSemantico(linea, columna, "return", "'return' solo puede usarse dentro de una función o método.");
            return;
        }
        if (valor == null) {
            gen.emitir("return", "Regresa al punto de llamada sin valor");
            return;
        }
        valor = gen.materializar(valor);
        Simbolo rutina = rutinas.peek();
        TipoDato tipo = valor.getTipo() == TipoDato.ERROR ? TipoDato.ANY : valor.getTipo();
        if (!rutina.retornaValor()) {
            rutina.setTipo(tipo);
            rutina.setNombreClase(valor.getClase());
            rutina.setRetornaValor(true);
        } else if (rutina.getTipo() != tipo) {
            rutina.setTipo(TipoDato.ANY); // retorna valores de distinto tipo
        }
        gen.emitir("return " + valor.getLugar(), (valor.esConstante() ? "Retorna la constante " : "Retorna el valor de ")
                + valor.getLugar());
    }

    /** Cada argumento se guarda en una etiqueta temporal antes de la llamada. */
    public NodoExpresion argumento(NodoExpresion valor, int linea) {
        gen.marcarLinea(linea);
        valor = gen.materializar(valor);
        if (valor.esTemporal()) {
            return valor;
        }
        String t = gen.nuevoTemporal();
        gen.emitir(t + " = " + valor.getLugar(), t + " es una etiqueta temporal para un parámetro de la llamada");
        return NodoExpresion.temporal(valor.getTipo(), t, valor.getClase());
    }

    /** Condición que no pudo analizarse por un error sintáctico. */
    public NodoExpresion condicionErronea() {
        return nodoError("?");
    }

    private NodoExpresion nodoError(String lugar) {
        return NodoExpresion.variable(TipoDato.ERROR, lugar, null, null);
    }

    /** Emite "call L" y deja el valor de retorno en un temporal. */
    private NodoExpresion emitirLlamada(Simbolo rutina, List<NodoExpresion> argumentos, String descripcion,
                                        int linea, int columna) {
        validarArgumentos(rutina, argumentos.size(), linea, columna);
        gen.emitir("call " + rutina.getEtiqueta(), comentarioLlamada(descripcion, rutina));
        return gen.emitirCaptura("return_value", rutina.getTipo(), rutina.getNombreClase(),
                "es una etiqueta temporal para el valor retornado por " + rutina.getNombre());
    }

    private void validarArgumentos(Simbolo rutina, int recibidos, int linea, int columna) {
        int esperados = rutina.getParametros().size();
        if (recibidos != esperados) {
            errorSemantico(linea, columna, rutina.getNombre(), "'" + rutina.getNombre() + "' requiere " + esperados
                    + " argumento(s) y se proporcionaron " + recibidos + ".");
        }
    }

    private String comentarioLlamada(String descripcion, Simbolo rutina) {
        return "Llama a " + descripcion + ", que inicia en la etiqueta " + rutina.getEtiqueta();
    }

    /**
     * La función o método aún no se ha definido: se emite "call ?" y la etiqueta
     * se completa cuando se conozca (al cerrar la clase o al terminar el programa).
     */
    private NodoExpresion emitirLlamadaPendiente(List<LlamadaPendiente> lista, String nombre, String descripcion,
                                                 int argumentos, int linea, int columna) {
        LlamadaPendiente pendiente = new LlamadaPendiente();
        pendiente.instruccion = gen.emitir("call ?", "Llamada pendiente de resolver");
        pendiente.nombre = nombre;
        pendiente.descripcion = descripcion;
        pendiente.argumentos = argumentos;
        pendiente.linea = linea;
        pendiente.columna = columna;
        lista.add(pendiente);
        return gen.emitirCaptura("return_value", TipoDato.ANY, null,
                "es una etiqueta temporal para el valor retornado por " + nombre);
    }

    private void resolverPendiente(LlamadaPendiente pendiente, Simbolo rutina, String mensajeError) {
        if (rutina == null) {
            gen.parchar(pendiente.instruccion, "call ?", "Llamada sin resolver: " + mensajeError);
            errorSemantico(pendiente.linea, pendiente.columna, pendiente.nombre, mensajeError);
            return;
        }
        validarArgumentos(rutina, pendiente.argumentos, pendiente.linea, pendiente.columna);
        gen.parchar(pendiente.instruccion, "call " + rutina.getEtiqueta(),
                comentarioLlamada(pendiente.descripcion, rutina));
    }

    /** Al terminar el análisis: resuelve las llamadas a funciones definidas después de usarse. */
    public void finPrograma() {
        for (LlamadaPendiente pendiente : funcionesPendientes) {
            Simbolo funcion = tabla.buscar(pendiente.nombre);
            if (funcion != null && funcion.getClase() != Simbolo.Clase.FUNCION) {
                funcion = null;
            }
            resolverPendiente(pendiente, funcion, "La función '" + pendiente.nombre + "' no ha sido declarada.");
        }
        funcionesPendientes.clear();
    }

    /** f(argumentos) */
    public NodoExpresion llamadaFuncion(String id, List<NodoExpresion> argumentos, int linea, int columna) {
        gen.marcarLinea(linea);
        Simbolo funcion = tabla.buscar(id);
        if (funcion == null) {
            // Puede estar definida más adelante (las funciones de JavaScript se elevan)
            return emitirLlamadaPendiente(funcionesPendientes, id, "la función " + id, argumentos.size(), linea, columna);
        }
        if (funcion.getClase() != Simbolo.Clase.FUNCION) {
            errorSemantico(linea, columna, id, "'" + id + "' es " + articulo(funcion) + " y no puede llamarse como función.");
            return nodoError(id);
        }
        return emitirLlamada(funcion, argumentos, "la función " + id, linea, columna);
    }

    // ==================================================================
    // Clases y objetos
    // ==================================================================

    public void inicioClase(String id, int linea, int columna) {
        gen.marcarLinea(linea);
        Simbolo clase = new Simbolo(id, Simbolo.Clase.CLASE, TipoDato.OBJECT, tabla.obtenerAmbitoActual(), linea, columna);
        clase.setNombreClase(id);
        if (!tabla.insertar(clase)) {
            errorSemantico(linea, columna, id, "El identificador '" + id + "' ya fue declarado en este ámbito.");
        }
        claseActual = clase;
        gen.emitirComentario("Definición de la clase " + id);
    }

    public void finClase() {
        for (LlamadaPendiente pendiente : metodosPendientes) {
            Simbolo metodo = claseActual.getMiembro(pendiente.nombre);
            if (metodo != null && metodo.getClase() != Simbolo.Clase.METODO) {
                metodo = null;
            }
            resolverPendiente(pendiente, metodo, "La clase '" + claseActual.getNombre()
                    + "' no tiene el método '" + pendiente.nombre + "'.");
        }
        metodosPendientes.clear();
        gen.emitirComentario("Fin de la clase " + claseActual.getNombre());
        claseActual = null;
    }

    private boolean miembroRepetido(String id, int linea, int columna) {
        if (claseActual.getMiembro(id) != null) {
            errorSemantico(linea, columna, id, "El miembro '" + id + "' ya fue declarado en la clase '"
                    + claseActual.getNombre() + "'.");
            return true;
        }
        return false;
    }

    private Simbolo nuevoAtributo(Simbolo clase, String id, TipoDato tipo, int linea, int columna) {
        Simbolo atributo = new Simbolo(id, Simbolo.Clase.ATRIBUTO, tipo, "clase " + clase.getNombre(), linea, columna);
        clase.agregarMiembro(atributo);
        tabla.registrar(atributo);
        return atributo;
    }

    /** Atributo de clase: "edad = 0;" o "edad;" (inicial es null). */
    public void atributo(String id, NodoExpresion inicial, int linea, int columna) {
        gen.marcarLinea(linea);
        if (miembroRepetido(id, linea, columna)) {
            return;
        }
        nuevoAtributo(claseActual, id, inicial == null ? TipoDato.UNDEFINED : inicial.getTipo(), linea, columna);
        gen.emitirComentario("Atributo " + id + (inicial == null ? " sin valor inicial"
                : " con valor inicial " + inicial.getLugar()));
    }

    public void inicioMetodo(String id, List<String> parametros, int linea, int columna) {
        gen.marcarLinea(linea);
        Simbolo metodo = new Simbolo(id, Simbolo.Clase.METODO, TipoDato.ANY, "clase " + claseActual.getNombre(), linea, columna);
        if (!miembroRepetido(id, linea, columna)) {
            claseActual.agregarMiembro(metodo);
            tabla.registrar(metodo);
        }
        String nombre = id.equals("constructor") ? "el constructor" : "el método " + id;
        inicioRutina(metodo, parametros, nombre + " de la clase " + claseActual.getNombre(),
                claseActual.getNombre() + "." + id, linea, columna);
    }

    /** Devuelve la clase del objeto, CLASE_DINAMICA si no se conoce, o null si hubo error. */
    private Simbolo claseDe(String objeto, int linea, int columna) {
        if (objeto.equals("this")) {
            if (claseActual == null || rutinas.isEmpty()) {
                errorSemantico(linea, columna, "this", "'this' solo puede usarse dentro de un método de clase.");
                return null;
            }
            return claseActual;
        }
        Simbolo simbolo = tabla.buscar(objeto);
        if (simbolo == null) {
            errorSemantico(linea, columna, objeto, "La variable '" + objeto + "' no ha sido declarada en un ámbito visible.");
            return null;
        }
        if (simbolo.getTipo() == TipoDato.ANY) {
            return CLASE_DINAMICA;
        }
        Simbolo clase = simbolo.esVariable() && simbolo.getNombreClase() != null
                ? tabla.buscar(simbolo.getNombreClase()) : null;
        if (simbolo.getTipo() != TipoDato.OBJECT || clase == null) {
            errorSemantico(linea, columna, objeto, "'" + objeto + "' no es un objeto (es de tipo '" + simbolo.getTipo() + "').");
            return null;
        }
        return clase;
    }

    /** Lectura de un atributo: objeto.miembro / this.miembro */
    public NodoExpresion usoMiembro(String objeto, String miembro, int linea, int columna) {
        String lugar = objeto + "." + miembro;
        Simbolo clase = claseDe(objeto, linea, columna);
        if (clase == null) {
            return nodoError(lugar);
        }
        if (clase == CLASE_DINAMICA) {
            return NodoExpresion.variable(TipoDato.ANY, lugar, null, null);
        }
        Simbolo atributo = clase.getMiembro(miembro);
        if (atributo == null || atributo.getClase() != Simbolo.Clase.ATRIBUTO) {
            errorSemantico(linea, columna, miembro, "La clase '" + clase.getNombre() + "' no tiene el atributo '" + miembro + "'.");
            return nodoError(lugar);
        }
        return NodoExpresion.variable(atributo.getTipo(), lugar, null, atributo.getNombreClase());
    }

    /** objeto.miembro = valor / this.miembro = valor */
    public void asignarMiembro(String objeto, String miembro, NodoExpresion valor, int linea, int columna) {
        gen.marcarLinea(linea);
        String lugar = objeto + "." + miembro;
        Simbolo clase = claseDe(objeto, linea, columna);
        if (clase == null) {
            return;
        }
        if (clase != CLASE_DINAMICA) {
            Simbolo atributo = clase.getMiembro(miembro);
            if (atributo == null && objeto.equals("this")) {
                // JavaScript permite crear atributos al asignarlos con this (típico del constructor)
                atributo = nuevoAtributo(clase, miembro, TipoDato.UNDEFINED, linea, columna);
            }
            if (atributo == null || atributo.getClase() != Simbolo.Clase.ATRIBUTO) {
                errorSemantico(linea, columna, miembro, "La clase '" + clase.getNombre() + "' no tiene el atributo '" + miembro + "'.");
                return;
            }
            validarAsignacion(atributo, lugar, valor, linea, columna);
        }
        emitirAsignacion(lugar, valor);
    }

    /** objeto.metodo(argumentos) / this.metodo(argumentos) / console.log(argumentos) */
    public NodoExpresion llamadaMetodo(String objeto, String metodo, List<NodoExpresion> argumentos, int linea, int columna) {
        gen.marcarLinea(linea);
        String lugar = objeto + "." + metodo;
        if (objeto.equals("console") && metodo.equals("log") && tabla.buscar("console") == null) {
            for (NodoExpresion a : argumentos) {
                gen.emitir("print " + a.getLugar(), "Muestra en consola el valor de " + a.getLugar());
            }
            return NodoExpresion.variable(TipoDato.UNDEFINED, "undefined", null, null);
        }
        Simbolo clase = claseDe(objeto, linea, columna);
        if (clase == null) {
            return nodoError(lugar);
        }
        if (clase == CLASE_DINAMICA) {
            errorSemantico(linea, columna, metodo, "No se puede resolver el método '" + metodo
                    + "' porque no se conoce la clase de '" + objeto + "'.");
            return nodoError(lugar);
        }
        Simbolo simbolo = clase.getMiembro(metodo);
        String descripcion = "el método " + metodo + " del objeto " + objeto;
        if (simbolo == null && clase == claseActual) {
            // El método puede estar definido más adelante en la misma clase
            return emitirLlamadaPendiente(metodosPendientes, metodo, descripcion, argumentos.size(), linea, columna);
        }
        if (simbolo == null || simbolo.getClase() != Simbolo.Clase.METODO) {
            errorSemantico(linea, columna, metodo, "La clase '" + clase.getNombre() + "' no tiene el método '" + metodo + "'.");
            return nodoError(lugar);
        }
        return emitirLlamada(simbolo, argumentos, descripcion, linea, columna);
    }

    /** new Clase(argumentos) */
    public NodoExpresion nuevoObjeto(String nombreClase, List<NodoExpresion> argumentos, int linea, int columna) {
        gen.marcarLinea(linea);
        Simbolo clase = tabla.buscar(nombreClase);
        if (clase == null || clase.getClase() != Simbolo.Clase.CLASE) {
            errorSemantico(linea, columna, nombreClase, "La clase '" + nombreClase + "' no ha sido declarada.");
            return nodoError("new " + nombreClase);
        }
        NodoExpresion objeto = gen.emitirCaptura("create " + nombreClase, TipoDato.OBJECT, nombreClase,
                "es una etiqueta temporal para el nuevo objeto " + nombreClase);
        Simbolo constructor = clase.getMiembro("constructor");
        if (constructor != null && constructor.getClase() == Simbolo.Clase.METODO) {
            gen.descartarValorDeRetorno(emitirLlamada(constructor, argumentos,
                    "el constructor de " + nombreClase, linea, columna));
        } else if (!argumentos.isEmpty()) {
            errorSemantico(linea, columna, nombreClase, "La clase '" + nombreClase
                    + "' no tiene constructor y se proporcionaron " + argumentos.size() + " argumento(s).");
        }
        return objeto;
    }

    /** Llamada usada como sentencia: su valor de retorno no se guarda. */
    public void descartar(NodoExpresion llamada) {
        gen.descartarValorDeRetorno(llamada);
    }
}

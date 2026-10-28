package act11;

import java.util.ArrayList;
import java.util.List;

public class ControladorSemantico {
    private TablaSimbolos entornoActual;
    private List<String> errores;
    private int nivelCiclos;
    private Simbolo funcionActual;
    private Simbolo claseActual;

    public ControladorSemantico() {
        this.entornoActual = new TablaSimbolos("global", TablaSimbolos.TipoAmbito.GLOBAL, null);
        this.errores = new ArrayList<>();
        this.nivelCiclos = 0;
        this.funcionActual = null;
        this.claseActual = null;
    }

    public void agregarError(int linea, int columna, String token, String causa) {
        String err = String.format("Error semántico en línea %d, columna %d: [%s] -> %s",
                linea, columna, token, causa);
        errores.add(err);
        System.err.println(err);
    }

    public boolean hayErrores() { return !errores.isEmpty(); }
    public List<String> getErrores() { return errores; }
    public TablaSimbolos getEntornoActual() { return entornoActual; }

    public void entrarAmbito(String nombre, TablaSimbolos.TipoAmbito tipo) {
        entornoActual = new TablaSimbolos(nombre, tipo, entornoActual);
    }

    public void salirAmbito() {
        if (entornoActual.getAnterior() != null) {
            entornoActual = entornoActual.getAnterior();
        }
    }

    public void entrarCiclo() { nivelCiclos++; }
    public void salirCiclo() { if (nivelCiclos > 0) nivelCiclos--; }

    public void validarCondicion(TipoDato tipo, int linea, int col, String estructura) {
        if (tipo != TipoDato.BOOLEAN && tipo != TipoDato.ERROR) {
            agregarError(linea, col, estructura,
                    "La condición de '" + estructura + "' debe ser de tipo 'boolean'; se encontró '" + tipo + "'.");
        }
    }

    public void validarBreak(int linea, int col) {
        if (nivelCiclos <= 0) {
            agregarError(linea, col, "break",
                    "La instrucción 'break' no se encuentra dentro de un ciclo o estructura de iteración.");
        }
    }

    public void validarContinue(int linea, int col) {
        if (nivelCiclos <= 0) {
            agregarError(linea, col, "continue",
                    "La instrucción 'continue' debe utilizarse dentro de una estructura de iteración.");
        }
    }

    public void setFuncionActual(Simbolo f) { this.funcionActual = f; }
    public Simbolo getFuncionActual() { return funcionActual; }

    public void validarReturn(TipoDato tipoExpr, int linea, int col) {
        if (funcionActual == null) {
            agregarError(linea, col, "return",
                    "La instrucción 'return' no puede utilizarse fuera de una función o método.");
            return;
        }
        TipoDato esperado = funcionActual.getTipoRetorno();
        if (esperado == TipoDato.VOID && tipoExpr != TipoDato.VOID) {
            agregarError(linea, col, "return",
                    "La función/método '" + funcionActual.getNombre() + "' es de tipo 'void' y no debe retornar un valor.");
        } else if (esperado != TipoDato.VOID && !esperado.esCompatible(tipoExpr)) {
            agregarError(linea, col, "return",
                    "El método/función '" + funcionActual.getNombre() + "' declara un retorno de tipo '"
                    + esperado + "', pero la expresión retornada es de tipo '" + tipoExpr + "'.");
        }
    }

    public void validarDeclaracionVariable(String nombre, TipoDato tipo, Simbolo.Rol rol, int linea, int col) {
        Simbolo existente = entornoActual.buscarLocal(nombre);
        if (existente != null) {
            agregarError(linea, col, nombre,
                    "El identificador '" + nombre + "' ya está declarado en el ámbito actual.");
            return;
        }
        entornoActual.insertar(new Simbolo(nombre, tipo, rol, linea, col));
    }

    public TipoDato validarUsoVariable(String nombre, int linea, int col) {
        Simbolo s = entornoActual.buscar(nombre);
        if (s == null && claseActual != null && claseActual.getEntornoClase() != null) {
            s = claseActual.getEntornoClase().buscarLocal(nombre);
        }
        if (s == null) {
            agregarError(linea, col, nombre,
                    "El identificador '" + nombre + "' no está disponible o no ha sido declarado en el ámbito actual.");
            return TipoDato.ERROR;
        }
        return s.getTipo();
    }

    public TipoDato validarLlamadaFuncion(String nombre, List<TipoDato> tiposArgs, int linea, int col) {
        Simbolo s = entornoActual.buscar(nombre);
        if (s == null && claseActual != null && claseActual.getEntornoClase() != null) {
            s = claseActual.getEntornoClase().buscarLocal(nombre);
        }
        if (s == null || (s.getRol() != Simbolo.Rol.FUNCION && s.getRol() != Simbolo.Rol.METODO)) {
            agregarError(linea, col, nombre,
                    "La función o método '" + nombre + "' no está declarado o no puede resolverse en este contexto.");
            return TipoDato.ERROR;
        }

        List<Simbolo> params = s.getParametros();
        if (params.size() != tiposArgs.size()) {
            agregarError(linea, col, nombre,
                    "El método '" + nombre + "' requiere " + params.size() +
                    " argumentos, pero se proporcionaron " + tiposArgs.size() + ".");
            return s.getTipoRetorno();
        }

        for (int i = 0; i < params.size(); i++) {
            TipoDato tipoEsperado = params.get(i).getTipo();
            TipoDato tipoRecibido = tiposArgs.get(i);
            if (!tipoEsperado.esCompatible(tipoRecibido)) {
                agregarError(linea, col, nombre,
                        "El argumento " + (i + 1) + " del método '" + nombre + "' es de tipo '" +
                        tipoRecibido + "'; se esperaba un tipo compatible con '" + tipoEsperado + "'.");
            }
        }
        return s.getTipoRetorno();
    }

    public void setClaseActual(Simbolo c) { this.claseActual = c; }

    public void declararClase(String nombre, int linea, int col) {
        if (entornoActual.buscarLocal(nombre) != null) {
            agregarError(linea, col, nombre, "La clase '" + nombre + "' ya ha sido declarada en este ámbito.");
            return;
        }
        Simbolo claseSim = new Simbolo(nombre, TipoDato.OBJECT, Simbolo.Rol.CLASE, linea, col);
        claseSim.setEntornoClase(new TablaSimbolos(nombre, TablaSimbolos.TipoAmbito.CLASE, entornoActual));
        entornoActual.insertar(claseSim);
        setClaseActual(claseSim);
        entornoActual = claseSim.getEntornoClase();
    }

    public TipoDato validarAccesoMiembro(String nombreObjeto, String nombreMiembro, int linea, int col) {
        Simbolo obj = entornoActual.buscar(nombreObjeto);
        if (obj == null) {
            agregarError(linea, col, nombreObjeto, "El identificador o referencia '" + nombreObjeto + "' no está disponible o no ha sido declarado.");
            return TipoDato.ERROR;
        }
        if (obj.getEntornoClase() != null) {
            Simbolo m = obj.getEntornoClase().buscarLocal(nombreMiembro);
            if (m == null) {
                agregarError(linea, col, nombreMiembro, "El miembro o atributo '" + nombreMiembro + "' no está declarado en la clase.");
                return TipoDato.ERROR;
            }
            return m.getTipo();
        }
        return TipoDato.ANY;
    }
}
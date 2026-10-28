package act11;

import java.util.ArrayList;
import java.util.List;

public class Simbolo {
    public enum Rol {
        VARIABLE,
        PARAMETRO,
        FUNCION,
        METODO,
        CLASE,
        ATRIBUTO
    }

    private String nombre;
    private TipoDato tipo;
    private Rol rol;
    private int linea;
    private int columna;
    private List<Simbolo> parametros;
    private TipoDato tipoRetorno;
    private TablaSimbolos entornoClase;

    public Simbolo(String nombre, TipoDato tipo, Rol rol, int linea, int columna) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.rol = rol;
        this.linea = linea;
        this.columna = columna;
        this.parametros = new ArrayList<>();
    }

    public Simbolo(String nombre, TipoDato tipoRetorno, List<Simbolo> parametros, Rol rol, int linea, int columna) {
        this(nombre, tipoRetorno, rol, linea, columna);
        this.tipoRetorno = tipoRetorno;
        this.parametros = (parametros != null) ? parametros : new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public TipoDato getTipo() { return tipo; }
    public void setTipo(TipoDato tipo) { this.tipo = tipo; }
    public Rol getRol() { return rol; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    public List<Simbolo> getParametros() { return parametros; }
    public TipoDato getTipoRetorno() { return tipoRetorno; }
    public TablaSimbolos getEntornoClase() { return entornoClase; }
    public void setEntornoClase(TablaSimbolos entornoClase) { this.entornoClase = entornoClase; }
}
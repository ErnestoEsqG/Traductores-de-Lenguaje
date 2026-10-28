package act11;

public enum TipoDato {
    NUMBER,
    BOOLEAN,
    STRING,
    VOID,
    OBJECT,
    ANY,
    ERROR;

    public boolean esCompatible(TipoDato otro) {
        if (this == ERROR || otro == ERROR) return false;
        if (this == ANY || otro == ANY) return true;
        return this == otro;
    }
}
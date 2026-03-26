package batallaflota.model;

import java.io.Serializable;

public class Coordenada implements Serializable {
    private static final long serialVersionUID = 1L;

    private int fila;
    private int columna;

    public Coordenada(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila()    { return fila; }
    public int getColumna() { return columna; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordenada que = (Coordenada) o;
        return fila == que.fila && columna == que.columna;
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(fila, columna); }

    @Override
    public String toString() { return String.format("(%d,%d)", fila, columna); }
}
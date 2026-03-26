package batallaflota.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import batallaflota.utils.Constants;

public class Tauler implements Serializable {
    private static final long serialVersionUID = 1L;

    private int[][] tauler;
    private int[][] vaixellsOcupacio;
    private Set<Coordenada> disparosRebuts;
    private Set<Coordenada> disparosEfectuats;
    private String nomJugador;
    private boolean victoria;
    private int vaixellsEnfonsats;
    private int vaixellsTotal;

    public Tauler(String nomJugador) {
        this.nomJugador = nomJugador;
        this.tauler = new int[Constants.TAULER_MIDA][Constants.TAULER_MIDA];
        this.vaixellsOcupacio = new int[Constants.TAULER_MIDA][Constants.TAULER_MIDA];
        this.disparosRebuts = new HashSet<>();
        this.disparosEfectuats = new HashSet<>();
        this.victoria = false;
        this.vaixellsEnfonsats = 0;
        this.vaixellsTotal = Constants.VAIXELL_MIDES.length;

        VaixellsAleatoris();
    }

    private void VaixellsAleatoris() {
        Random aleatori = new Random();
        int idVaixell = 1;

        for (int mida : Constants.VAIXELL_MIDES) {
            boolean colocat = false;
            int intents = 0;
            while (!colocat && intents < 1000) {
                int fila = aleatori.nextInt(Constants.TAULER_MIDA);
                int columna = aleatori.nextInt(Constants.TAULER_MIDA);
                boolean horiz = aleatori.nextBoolean();
                if (potColocar(fila, columna, mida, horiz)) {
                    colocarVaixell(fila, columna, mida, horiz, idVaixell);
                    colocat = true;
                }
                intents++;
            }
            if (colocat) idVaixell++;
        }
    }

    private boolean potColocar(int fila, int columna, int mida, boolean horitzontal) {
        if (horitzontal) {
            if (columna + mida > Constants.TAULER_MIDA) return false;
            for (int i = 0; i < mida; i++)
                if (tauler[fila][columna + i] != 0) return false;
        } else {
            if (fila + mida > Constants.TAULER_MIDA) return false;
            for (int i = 0; i < mida; i++)
                if (tauler[fila + i][columna] != 0) return false;
        }
        return true;
    }

    private void colocarVaixell(int fila, int columna, int mida, boolean horitzontal, int idVaixell) {
        if (horitzontal) {
            for (int i = 0; i < mida; i++) {
                tauler[fila][columna + i] = 1;
                vaixellsOcupacio[fila][columna + i] = idVaixell;
            }
        } else {
            for (int i = 0; i < mida; i++) {
                tauler[fila + i][columna] = 1;
                vaixellsOcupacio[fila + i][columna] = idVaixell;
            }
        }
    }

    public synchronized String processaDisparo(Coordenada coordenada) {
        int fila = coordenada.getFila();
        int columna = coordenada.getColumna();

        if (disparosRebuts.contains(coordenada)) return "JA_DISPARAT";
        disparosRebuts.add(coordenada);

        if (tauler[fila][columna] == 1) {
            tauler[fila][columna] = -1;
            int idVaixell = vaixellsOcupacio[fila][columna];
            if (vaixellEstaEnfonsat(idVaixell)) {
                vaixellsEnfonsats++;
                if (vaixellsEnfonsats == vaixellsTotal) victoria = true;
                return "ENFONSAT";
            }
            return "TOCA";
        }

        tauler[fila][columna] = -2;
        return "AIGUA";
    }

    private boolean vaixellEstaEnfonsat(int idVaixell) {
        for (int i = 0; i < Constants.TAULER_MIDA; i++)
            for (int j = 0; j < Constants.TAULER_MIDA; j++)
                if (vaixellsOcupacio[i][j] == idVaixell && tauler[i][j] == 1)
                    return false;
        return true;
    }

    public synchronized Coordenada disparoAleatorio() {
        Random aleatori = new Random();
        int fila, columna;
        Coordenada c;
        do {
            fila = aleatori.nextInt(Constants.TAULER_MIDA);
            columna = aleatori.nextInt(Constants.TAULER_MIDA);
            c = new Coordenada(fila, columna);
        } while (disparosEfectuats.contains(c));
        disparosEfectuats.add(c);
        return c;
    }

    public String getTaulerVisualCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== TAULER DE ").append(nomJugador).append(" (COMPLET) ===\n");
        sb.append("   0 1 2 3 4 5 6 7 8 9\n");
        for (int i = 0; i < Constants.TAULER_MIDA; i++) {
            sb.append(i).append("  ");
            for (int j = 0; j < Constants.TAULER_MIDA; j++) {
                switch (tauler[i][j]) {
                    case  0: sb.append(". "); break;
                    case  1: sb.append("V "); break;
                    case -1: sb.append("X "); break;
                    case -2: sb.append("~ "); break;
                }
            }
            sb.append("\n");
        }
        sb.append("Llegenda: V=Vaixell, X=Tocat, ~=Aigua, .=Lliure\n");
        sb.append("Tirs rebuts: ").append(disparosRebuts.size()).append("\n");
        sb.append("Vaixells enfonsats: ").append(vaixellsEnfonsats)
                .append("/").append(vaixellsTotal).append("\n");
        return sb.toString();
    }

    public String getTaulerVisual() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== TAULER DE ").append(nomJugador).append(" ===\n");
        sb.append("   0 1 2 3 4 5 6 7 8 9\n");
        for (int i = 0; i < Constants.TAULER_MIDA; i++) {
            sb.append(i).append("  ");
            for (int j = 0; j < Constants.TAULER_MIDA; j++) {
                switch (tauler[i][j]) {
                    case  0: sb.append(". "); break;
                    case  1: sb.append(". "); break;
                    case -1: sb.append("X "); break;
                    case -2: sb.append("~ "); break;
                }
            }
            sb.append("\n");
        }
        sb.append("Llegenda: X=Tocat, ~=Aigua, .=Desconegut\n");
        sb.append("Tirs rebuts: ").append(disparosRebuts.size()).append("\n");
        sb.append("Vaixells enfonsats: ").append(vaixellsEnfonsats)
                .append("/").append(vaixellsTotal).append("\n");
        return sb.toString();
    }

    public synchronized boolean isVictoria() { return victoria; }
    public String getNomJugador() { return nomJugador; }
}
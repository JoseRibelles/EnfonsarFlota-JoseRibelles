package batallaflota.model;

import java.io.Serializable;

public class ResultatTir implements Serializable {
    private static final long serialVersionUID = 1L;

    private String resultado;
    private String taulerJugador;
    private String taulerServidor;
    private boolean victoriaServidor;
    private boolean victoriaJugador;

    public ResultatTir(String resultado, String taulerJugador, String taulerServidor, boolean victoriaServidor, boolean victoriaJugador) {
        this.resultado = resultado;
        this.taulerJugador = taulerJugador;
        this.taulerServidor = taulerServidor;
        this.victoriaServidor = victoriaServidor;
        this.victoriaJugador = victoriaJugador;
    }

    public String getResultado()        { return resultado; }
    public String getTaulerJugador()    { return taulerJugador; }
    public String getTaulerServidor()   { return taulerServidor; }
    public boolean isVictoriaServidor() { return victoriaServidor; }
    public boolean isVictoriaJugador()  { return victoriaJugador; }
}
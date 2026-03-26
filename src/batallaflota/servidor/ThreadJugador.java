package batallaflota.servidor;

import batallaflota.model.Coordenada;
import batallaflota.model.ResultatTir;
import batallaflota.model.Tauler;

import java.io.*;
import java.net.Socket;

public class ThreadJugador extends Thread {

    private Socket socketClient;
    private Tauler taulerServidor;
    private Tauler taulerJugador;
    private ServidorBatallaFlota servidor;
    private ObjectOutputStream sortida;
    private ObjectInputStream entrada;
    private String nomJugador;

    public ThreadJugador(Socket socketClient, Tauler taulerServidor, ServidorBatallaFlota servidor) {
        this.socketClient = socketClient;
        this.taulerServidor = taulerServidor;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try {
            sortida = new ObjectOutputStream(socketClient.getOutputStream());
            sortida.flush();
            entrada = new ObjectInputStream(socketClient.getInputStream());

            nomJugador = (String) entrada.readObject();
            System.out.println("[SERVER] Jugador connectat: " + nomJugador);

            taulerJugador = new Tauler(nomJugador);

            String benvinguda = "Benvingut " + nomJugador + " a Batalla Flota!\n"
                    + "Els teus vaixells:\n"
                    + taulerJugador.getTaulerVisualCompleto();
            sortida.writeObject(benvinguda);
            sortida.flush();

            boolean jocActiu = true;
            while (jocActiu) {
                try {
                    Coordenada coordenadaJugador = (Coordenada) entrada.readObject();
                    System.out.println("[SERVER] " + nomJugador + " dispara a " + coordenadaJugador);

                    String resultadoJugador = taulerServidor.processaDisparo(coordenadaJugador);
                    System.out.println("[SERVER] Resultat: " + resultadoJugador);

                    Coordenada coordenadaMaquina = taulerJugador.disparoAleatorio();
                    String resultadoMaquina      = taulerJugador.processaDisparo(coordenadaMaquina);
                    System.out.println("[SERVER] Maquina dispara a " + coordenadaMaquina
                            + " => " + resultadoMaquina);

                    boolean victoriaJugador = taulerServidor.isVictoria();
                    boolean victoriaMaquina = taulerJugador.isVictoria();

                    ResultatTir resultado = new ResultatTir(
                            resultadoJugador,
                            taulerJugador.getTaulerVisual(),
                            taulerServidor.getTaulerVisual(),
                            victoriaMaquina,
                            victoriaJugador
                    );

                    sortida.writeObject(resultado);
                    sortida.flush();


                    System.out.println(taulerServidor.getTaulerVisualCompleto());
                    System.out.println(taulerJugador.getTaulerVisualCompleto());

                    if (victoriaJugador) {
                        System.out.println("[SERVER] " + nomJugador + " HA GUANYAT!");
                        jocActiu = false;
                    } else if (victoriaMaquina) {
                        System.out.println("[SERVER] LA MAQUINA HA GUANYAT contra " + nomJugador);
                        jocActiu = false;
                    }

                } catch (EOFException e) {
                    System.out.println("[SERVER] " + nomJugador + " s'ha desconnectat");
                    jocActiu = false;
                }
            }

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("[SERVER] Error amb jugador " + nomJugador + ": " + ex.getMessage());
        } finally {
            tanca();
        }
    }

    private void tanca() {
        try {
            if (socketClient != null && !socketClient.isClosed())
                socketClient.close();
        } catch (IOException ex) {
            System.err.println("[SERVER] Error tancant socket: " + ex.getMessage());
        }
        servidor.eliminaJugador(this);
    }

    public String getNomJugador() { return nomJugador; }
}
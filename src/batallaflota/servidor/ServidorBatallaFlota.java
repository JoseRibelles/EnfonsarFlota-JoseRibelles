package batallaflota.servidor;

import batallaflota.model.Tauler;
import batallaflota.utils.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServidorBatallaFlota {

    private int port;
    private ServerSocket socketServidor;
    private List<ThreadJugador> jugadors;
    private Tauler taulerServidor;
    private boolean enExecucio;

    public ServidorBatallaFlota(int port) {
        this.port     = port;
        this.jugadors = new ArrayList<>();
        this.enExecucio = true;
    }

    public void inicialitza() throws IOException {
        socketServidor = new ServerSocket(port);
        taulerServidor = new Tauler("SERVIDOR");

        System.out.println("=== SERVIDOR BATALLA FLOTA ===");
        System.out.println("Escoltant al port " + port);
        System.out.println(taulerServidor.getTaulerVisualCompleto());
    }

    public void escolta() {
        System.out.println("Esperant jugadors...");
        try {
            while (enExecucio) {
                Socket socketClient = socketServidor.accept();
                System.out.println("[SERVER] Nova connexio: " + socketClient.getInetAddress());

                if (taulerServidor.isVictoria()) {
                    System.out.println("[SERVER] Reiniciant tauler del servidor per nova partida");
                    taulerServidor = new Tauler("SERVIDOR");
                    System.out.println(taulerServidor.getTaulerVisualCompleto());
                }

                ThreadJugador thread = new ThreadJugador(socketClient, taulerServidor, this);
                jugadors.add(thread);
                thread.start();

                System.out.println("[SERVER] Jugadors actius: " + jugadors.size());
            }
        } catch (IOException ex) {
            if (enExecucio) System.err.println("[SERVER] Error: " + ex.getMessage());
        } finally {
            tanca();
        }
    }

    public synchronized void eliminaJugador(ThreadJugador jugador) {
        jugadors.remove(jugador);
        System.out.println("[SERVER] Jugador desconnectat. Actius: " + jugadors.size());
    }

    public void tanca() {
        try {
            enExecucio = false;
            if (socketServidor != null && !socketServidor.isClosed())
                socketServidor.close();
            System.out.println("[SERVER] Servidor tancat");
        } catch (IOException ex) {
            System.err.println("[SERVER] Error tancant: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            ServidorBatallaFlota servidor = new ServidorBatallaFlota(Constants.PORT_TCP);
            servidor.inicialitza();
            servidor.escolta();
        } catch (IOException ex) {
            System.err.println("Error fatal: " + ex.getMessage());
        }
    }
}
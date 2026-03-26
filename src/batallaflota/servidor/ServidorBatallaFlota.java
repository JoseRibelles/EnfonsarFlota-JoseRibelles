package batallaflota.servidor;

import batallaflota.client.ColorTerminal;
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
        this.port = port;
        this.jugadors = new ArrayList<>();
        this.enExecucio = true;
    }

    public void inicialitza() throws IOException {
        socketServidor = new ServerSocket(port);
        taulerServidor = new Tauler("SERVIDOR");

        printCabecera();
        printInfo("Escoltant al port " + port);
        printSeparador();
        System.out.println(taulerServidor.getTaulerVisualCompleto());
        printSeparador();
    }

    public void escolta() {
        printInfo("Esperant jugadors...");
        try {
            while (enExecucio) {
                Socket socketClient = socketServidor.accept();
                printSuccess("[SERVER] Nova connexio: " + socketClient.getInetAddress());

                if (taulerServidor.isVictoria()) {
                    printWarning("[SERVER] Reiniciant tauler del servidor per nova partida");
                    taulerServidor = new Tauler("SERVIDOR");
                    System.out.println(taulerServidor.getTaulerVisualCompleto());
                }

                ThreadJugador thread = new ThreadJugador(socketClient, taulerServidor, this);
                jugadors.add(thread);
                thread.start();

                printInfo("[SERVER] Jugadors actius: " + ColorTerminal.YELLOW + jugadors.size() + ColorTerminal.RESET);
            }
        } catch (IOException ex) {
            if (enExecucio) {
                printError("[SERVER] Error: " + ex.getMessage());
            }
        } finally {
            tanca();
        }
    }

    public synchronized void eliminaJugador(ThreadJugador jugador) {
        jugadors.remove(jugador);
        printWarning("[SERVER] Jugador desconnectat: " + jugador.getNomJugador() + " | Actius: " + jugadors.size());
    }

    public void tanca() {
        try {
            enExecucio = false;
            if (socketServidor != null && !socketServidor.isClosed()) {
                socketServidor.close();
            }
            printInfo("[SERVER] Servidor tancat");
        } catch (IOException ex) {
            printError("[SERVER] Error tancant: " + ex.getMessage());
        }
    }

    private void printCabecera() {
        System.out.println(ColorTerminal.CYAN + "╔════════════════════════════════════╗" + ColorTerminal.RESET);
        System.out.println(ColorTerminal.CYAN + "║  🚢 SERVIDOR BATALLA FLOTA 🚢     ║" + ColorTerminal.RESET);
        System.out.println(ColorTerminal.CYAN + "╚════════════════════════════════════╝" + ColorTerminal.RESET);
    }

    private void printInfo(String mensaje) {
        System.out.println(ColorTerminal.BLUE + "ℹ️  " + mensaje + ColorTerminal.RESET);
    }

    private void printSuccess(String mensaje) {
        System.out.println(ColorTerminal.GREEN + "✅ " + mensaje + ColorTerminal.RESET);
    }

    private void printWarning(String mensaje) {
        System.out.println(ColorTerminal.YELLOW + "⚠️  " + mensaje + ColorTerminal.RESET);
    }

    private void printError(String mensaje) {
        System.err.println(ColorTerminal.RED + "❌ " + mensaje + ColorTerminal.RESET);
    }

    private void printSeparador() {
        System.out.println(ColorTerminal.CYAN + "════════════════════════════════════════" + ColorTerminal.RESET);
    }

    public static void main(String[] args) {
        try {
            ServidorBatallaFlota servidor = new ServidorBatallaFlota(Constants.PORT_TCP);
            servidor.inicialitza();
            servidor.escolta();
        } catch (IOException ex) {
            System.err.println(ColorTerminal.RED + "❌ Error fatal: " + ex.getMessage() + ColorTerminal.RESET);
            ex.printStackTrace();
        }
    }
}
package batallaflota.client;

import batallaflota.model.Coordenada;
import batallaflota.model.ResultatTir;
import batallaflota.utils.Constants;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientBatallaFlota extends Thread {

    private String maquina;
    private int port;
    private String nomJugador;
    private boolean enJoc;
    private Socket socket;
    private ObjectOutputStream sortida;
    private ObjectInputStream entrada;
    private Scanner escaner;

    public ClientBatallaFlota(String maquina, int port, String nomJugador) {
        this.maquina = maquina;
        this.port = port;
        this.nomJugador = nomJugador;
        this.escaner = new Scanner(System.in);
        this.enJoc = true;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(InetAddress.getByName(maquina), port);
            System.out.println("[CLIENT] Connectat a " + maquina + ":" + port);

            sortida = new ObjectOutputStream(socket.getOutputStream());
            sortida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());

            sortida.writeObject(nomJugador);
            sortida.flush();

            String benvinguda = (String) entrada.readObject();
            System.out.println("\n" + benvinguda);

            while (enJoc) {
                try {
                    int fila = llegirEnter("Introdueix fila (0-9): ");
                    int columna = llegirEnter("Introdueix columna (0-9): ");

                    if (fila < 0 || fila >= Constants.TAULER_MIDA
                            || columna < 0 || columna >= Constants.TAULER_MIDA) {
                        System.out.println("Coordenades invalides! Torna a intentar-ho.");
                        continue;
                    }

                    Coordenada coordenada = new Coordenada(fila, columna);

                    sortida.writeObject(coordenada);
                    sortida.flush();
                    
                    ResultatTir resultado = (ResultatTir) entrada.readObject();

                    System.out.println("\n========================================");
                    System.out.println("TU dispares a " + coordenada + " => " + resultado.getResultado());
                    System.out.println("--- Tauler del SERVIDOR (el que atacs) ---");
                    System.out.println(resultado.getTaulerServidor());
                    System.out.println("--- El TEU tauler (la maquina t'ha atacat) ---");
                    System.out.println(resultado.getTaulerJugador());
                    System.out.println("========================================\n");

                    if (resultado.isVictoriaJugador()) {
                        System.out.println(">>> HAS GUANYAT! Has enfonsat tota la flota del servidor! <<<");
                        enJoc = false;
                    } else if (resultado.isVictoriaServidor()) {
                        System.out.println(">>> LA MAQUINA HA GUANYAT! Ha enfonsat tots els teus vaixells! <<<");
                        enJoc = false;
                    }

                } catch (EOFException e) {
                    System.out.println("[CLIENT] Servidor desconnectat");
                    enJoc = false;
                }
            }

        } catch (Exception ex) {
            System.err.println("[CLIENT] Error: " + ex.getMessage());
        } finally {
            tanca();
        }
    }

    private int llegirEnter(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String linia = escaner.nextLine().trim();
                return Integer.parseInt(linia);
            } catch (NumberFormatException e) {
                System.out.println("Introdueix un numero entre 0 i 9.");
            }
        }
    }

    private void tanca() {
        try {
            enJoc = false;
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ex) {
            System.err.println("[CLIENT] Error tancant: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== BATALLA FLOTA - CLIENT ===");
        Scanner sc = new Scanner(System.in);

        System.out.print("Nom del jugador: ");
        String nom = sc.nextLine().trim();

        System.out.print("Adreca servidor (Enter = localhost): ");
        String maquina = sc.nextLine().trim();
        if (maquina.isEmpty()) maquina = Constants.HOST_LOCAL;

        ClientBatallaFlota client = new ClientBatallaFlota(maquina, Constants.PORT_TCP, nom);
        client.start();
    }
}
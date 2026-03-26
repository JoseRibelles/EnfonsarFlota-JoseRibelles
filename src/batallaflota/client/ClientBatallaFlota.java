package batallaflota.client;

import batallaflota.model.Coordenada;
import batallaflota.model.ResultatTir;
import batallaflota.utils.Constants;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientBatallaFlota extends Thread {

    private String maquina;
    private int port;
    private String nomJugador;
    private boolean enJoc;
    private Socket socket;
    private ObjectOutputStream sortida;
    private ObjectInputStream entrada;
    private static Scanner escaner;

    public ClientBatallaFlota(String maquina, int port, String nomJugador) {
        this.maquina = maquina;
        this.port = port;
        this.nomJugador = nomJugador;
        this.enJoc = true;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(InetAddress.getByName(maquina), port);
            printInfo("[CLIENT] Connectat a " + maquina + ":" + port);

            sortida = new ObjectOutputStream(socket.getOutputStream());
            sortida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());

            sortida.writeObject(nomJugador);
            sortida.flush();

            String benvinguda = (String) entrada.readObject();
            printSuccess("\n" + benvinguda);

            while (enJoc) {
                try {
                    int fila = llegirEnter("Introdueix fila (0-9): ");
                    int columna = llegirEnter("Introdueix columna (0-9): ");

                    if (fila < 0 || fila >= Constants.TAULER_MIDA
                            || columna < 0 || columna >= Constants.TAULER_MIDA) {
                        printWarning("Coordenades invalides! Torna a intentar-ho.");
                        continue;
                    }

                    Coordenada coordenada = new Coordenada(fila, columna);

                    sortida.writeObject(coordenada);
                    sortida.flush();

                    ResultatTir resultado = (ResultatTir) entrada.readObject();

                    printSeparador();
                    System.out.println(ColorTerminal.CYAN + "TU dispares a " + coordenada + " => " + ColorTerminal.YELLOW + resultado.getResultado() + ColorTerminal.RESET);
                    printSeparador();

                    System.out.println(ColorTerminal.BLUE + "--- Tauler del SERVIDOR (el que atacs) ---" + ColorTerminal.RESET);
                    System.out.println(resultado.getTaulerServidor());

                    System.out.println(ColorTerminal.PURPLE + "--- El TEU tauler (la maquina t'ha atacat) ---" + ColorTerminal.RESET);
                    System.out.println(resultado.getTaulerJugador());

                    printSeparador();

                    if (resultado.isVictoriaJugador()) {
                        printVictoria(">>> HAS GUANYAT! Has enfonsat tota la flota del servidor! <<<");
                        enJoc = false;
                    } else if (resultado.isVictoriaServidor()) {
                        printDerrota(">>> LA MAQUINA HA GUANYAT! Ha enfonsat tots els teus vaixells! <<<");
                        enJoc = false;
                    }

                } catch (EOFException e) {
                    printError("[CLIENT] Servidor desconnectat");
                    enJoc = false;
                } catch (NoSuchElementException e) {
                    printError("[CLIENT] Error llegint entrada");
                    enJoc = false;
                }
            }

        } catch (Exception ex) {
            printError("[CLIENT] Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            tanca();
        }
    }

    private synchronized int llegirEnter(String prompt) {
        while (true) {
            try {
                System.out.print(ColorTerminal.YELLOW + prompt + ColorTerminal.RESET);
                System.out.flush();

                if (!escaner.hasNextLine()) {
                    throw new EOFException("No input available");
                }

                String linia = escaner.nextLine().trim();

                if (linia.isEmpty()) {
                    printWarning("No pots deixar buit. Introdueix un numero entre 0 i 9.");
                    continue;
                }

                int valor = Integer.parseInt(linia);
                return valor;

            } catch (NumberFormatException e) {
                printWarning("Introdueix un numero valid entre 0 i 9.");
            } catch (NoSuchElementException e) {
                printError("Error llegint entrada");
                enJoc = false;
                break;
            } catch (EOFException e) {
                printError("Error: No input available");
                enJoc = false;
                break;
            }
        }
        return -1;
    }

    private void tanca() {
        try {
            enJoc = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            printError("[CLIENT] Error tancant: " + ex.getMessage());
        }
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

    private void printVictoria(String mensaje) {
        System.out.println(ColorTerminal.GREEN_BOLD + "\n🎉 " + mensaje + " 🎉\n" + ColorTerminal.RESET);
    }

    private void printDerrota(String mensaje) {
        System.out.println(ColorTerminal.RED_BOLD + "\n💀 " + mensaje + " 💀\n" + ColorTerminal.RESET);
    }

    private void printSeparador() {
        System.out.println(ColorTerminal.CYAN + "════════════════════════════════════════" + ColorTerminal.RESET);
    }

    public static void main(String[] args) {
        System.out.println(ColorTerminal.CYAN + "╔════════════════════════════════════╗" + ColorTerminal.RESET);
        System.out.println(ColorTerminal.CYAN + "║  🚢 BATALLA FLOTA - CLIENT 🚢     ║" + ColorTerminal.RESET);
        System.out.println(ColorTerminal.CYAN + "╚════════════════════════════════════╝" + ColorTerminal.RESET);

        escaner = new Scanner(System.in);

        try {
            System.out.print(ColorTerminal.YELLOW + "Nom del jugador: " + ColorTerminal.RESET);
            System.out.flush();
            String nom = escaner.nextLine().trim();

            if (nom.isEmpty()) {
                System.out.println(ColorTerminal.RED + "❌ El nom del jugador no pot estar buit!" + ColorTerminal.RESET);
                return;
            }

            System.out.print(ColorTerminal.YELLOW + "Adreca servidor (Enter = localhost): " + ColorTerminal.RESET);
            System.out.flush();
            String maquina = escaner.nextLine().trim();
            if (maquina.isEmpty()) maquina = Constants.HOST_LOCAL;

            ClientBatallaFlota client = new ClientBatallaFlota(maquina, Constants.PORT_TCP, nom);
            client.start();

            client.join();

        } catch (InterruptedException e) {
            System.err.println(ColorTerminal.RED + "❌ Thread interrumpido" + ColorTerminal.RESET);
            e.printStackTrace();
        } finally {
            if (escaner != null) {
                escaner.close();
            }
        }
    }
}
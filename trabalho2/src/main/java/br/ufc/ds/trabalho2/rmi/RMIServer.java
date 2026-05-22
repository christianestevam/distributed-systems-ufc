package br.ufc.ds.trabalho2.rmi;

import br.ufc.ds.trabalho2.app.InvestidorServiceImpl;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.Naming;

/**
 * Launcher that publishes InvestidorServiceImpl in the RMI registry.
 * Uses Java RMI (no manual sockets).
 */
public class RMIServer {
    private int port;

    public RMIServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            System.out.println("[RMIServer] Starting RMI registry on port " + port + "...");
            Registry registry = LocateRegistry.createRegistry(port);

            InvestidorServiceImpl service = new InvestidorServiceImpl();
            String name = "investidor_service";
            String url = String.format("rmi://localhost:%d/%s", port, name);
            Naming.rebind(url, service);

            System.out.println("[RMIServer] Service bound at " + url);
            System.out.println("[RMIServer] Server running — press Ctrl+C to stop.");

            // Keep server running
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            System.err.println("[RMIServer] Failed to start RMI server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
        RMIServer server = new RMIServer(port);
        server.start();
    }
}

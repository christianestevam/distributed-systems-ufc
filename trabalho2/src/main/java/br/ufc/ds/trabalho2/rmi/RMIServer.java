package br.ufc.ds.trabalho2.rmi;

import br.ufc.ds.trabalho2.app.InvestidorServiceImpl;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.Naming;
import java.net.InetAddress;

/**
 * Launcher that publishes InvestidorServiceImpl in the RMI registry.
 * Uses Java RMI (no manual sockets).
 */
public class RMIServer {
    private final String host;
    private final int port;

    public RMIServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            System.setProperty("java.rmi.server.hostname", host);
            System.out.println("[RMIServer] Using RMI hostname: " + host);
            System.out.println("[RMIServer] Starting RMI registry on port " + port + "...");
            Registry registry = LocateRegistry.createRegistry(port);

            InvestidorServiceImpl service = new InvestidorServiceImpl();
            String name = "investidor_service";
            String url = String.format("rmi://%s:%d/%s", host, port, name);
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
        String host;
        int port;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } else if (args.length == 1) {
            host = InetAddress.getLocalHost().getHostAddress();
            port = Integer.parseInt(args[0]);
        } else {
            host = InetAddress.getLocalHost().getHostAddress();
            port = 1099;
        }

        RMIServer server = new RMIServer(host, port);
        server.start();
    }
}

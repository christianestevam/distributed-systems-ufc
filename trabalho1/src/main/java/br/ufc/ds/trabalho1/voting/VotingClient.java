package br.ufc.ds.trabalho1.voting;

import java.io.*;
import java.net.*;
import java.util.*;

public class VotingClient {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: VotingClient <host> <port> <mode> [mcastAddr mcastPort]");
            System.out.println("mode: voter | admin");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String mode = args[2];
        String mcast = args.length >= 4 ? args[3] : "230.0.0.0";
        int mport = args.length >= 5 ? Integer.parseInt(args[4]) : 4446;

        if (mode.equalsIgnoreCase("voter")) {
            runVoter(host, port, mcast, mport);
        } else if (mode.equalsIgnoreCase("admin")) {
            runAdmin(host, port, mcast, mport);
        } else {
            System.out.println("Unknown mode: " + mode);
        }
    }

    private static void runVoter(String host, int port, String mcast, int mport) throws Exception {
        // start multicast listener
        Thread mthread = new Thread(() -> {
            try (MulticastSocket ms = new MulticastSocket(mport)) {
                InetAddress group = InetAddress.getByName(mcast);
                ms.joinGroup(group);
                byte[] buf = new byte[2048];
                while (true) {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    ms.receive(p);
                    String s = new String(p.getData(), 0, p.getLength(), "UTF-8");
                    System.out.println("[ADMIN NOTIFY] " + s);
                }
            } catch (IOException e) {
                System.out.println("Multicast listener stopped: " + e.getMessage());
            }
        }, "mcast-listener");
        mthread.setDaemon(true);
        mthread.start();

        try (Socket sock = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to voting server " + host + ":" + port);
            System.out.print("Enter username: ");
            String user = console.readLine().trim();
            out.println("LOGIN " + user);
            String resp = in.readLine();
            if (!"OK".equals(resp)) { System.out.println("Login failed: " + resp); return; }
            // read candidates
            System.out.println("Candidates:");
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("CAND_LIST")) continue;
                if (line.equals("END_LIST")) break;
                if (line.startsWith("CAND ")) {
                    System.out.println("  " + line.substring(5));
                }
            }

            System.out.println("Type: VOTE <id> | RESULTS | EXIT");
            while (true) {
                System.out.print("> ");
                String cmd = console.readLine();
                if (cmd == null) break;
                cmd = cmd.trim();
                if (cmd.equalsIgnoreCase("EXIT")) { out.println("QUIT"); break; }
                if (cmd.equalsIgnoreCase("RESULTS")) { out.println("RESULTS"); readResults(in); continue; }
                if (cmd.toUpperCase().startsWith("VOTE ")) {
                    out.println(cmd);
                    String r = in.readLine();
                    System.out.println(r);
                    continue;
                }
                System.out.println("Unknown command");
            }
        }
    }

    private static void readResults(BufferedReader in) throws IOException {
        String header = in.readLine();
        if (header == null) return;
        if (!header.startsWith("RESULTS")) { System.out.println(header); return; }
        System.out.println("Total votes: " + header.split(" ")[1]);
        String line;
        while ((line = in.readLine()) != null) {
            if (line.equals("END_RESULTS")) break;
            if (line.startsWith("RES ")) {
                System.out.println("  " + line.substring(4));
            }
        }
    }

    private static void runAdmin(String host, int port, String mcast, int mport) throws Exception {
        try (Socket sock = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("Admin password: ");
            String pwd = console.readLine().trim();
            out.println("ADMIN " + pwd);
            String resp = in.readLine();
            if (!resp.startsWith("OK")) { System.out.println("Auth failed: " + resp); return; }
            System.out.println("Admin mode. Commands: ADD <name> | REMOVE <id> | NOTIFY <message> | END | EXIT");
            String cmd;
            while ((cmd = console.readLine()) != null) {
                cmd = cmd.trim();
                if (cmd.equalsIgnoreCase("EXIT")) { out.println("QUIT"); break; }
                if (cmd.toUpperCase().startsWith("ADD ") || cmd.toUpperCase().startsWith("REMOVE ") || cmd.toUpperCase().startsWith("NOTIFY ") || cmd.equalsIgnoreCase("END")) {
                    out.println(cmd);
                    String r = in.readLine();
                    System.out.println(r);
                    continue;
                }
                System.out.println("Unknown command");
            }
        }
    }
}

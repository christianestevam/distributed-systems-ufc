package br.ufc.ds.trabalho1.voting;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VotingServer {

    private final int tcpPort;
    private final String mcastAddr;
    private final int mcastPort;
    private final long votingEndEpochMillis;

    private final Map<Integer, String> candidates = new ConcurrentHashMap<>();
    private final Map<Integer, Long> votes = new ConcurrentHashMap<>();
    private final Set<String> voters = ConcurrentHashMap.newKeySet();
    private volatile boolean votingOpen = true;
    private final AtomicInteger nextCandidateId = new AtomicInteger(1);

    private final String adminPassword = "adminpass";

    public VotingServer(int tcpPort, String mcastAddr, int mcastPort, long votingDurationSeconds) {
        this.tcpPort = tcpPort;
        this.mcastAddr = mcastAddr;
        this.mcastPort = mcastPort;
        this.votingEndEpochMillis = System.currentTimeMillis() + votingDurationSeconds * 1000L;
    }

    public void start() throws IOException {
        // start voting timeout watcher
        new Thread(this::votingWatcher, "Voting-Watcher").start();

        // start TCP server
        try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
            log("VotingServer listening on port " + tcpPort);
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new ClientHandler(client)).start();
            }
        }
    }

    private void votingWatcher() {
        while (votingOpen) {
            long now = System.currentTimeMillis();
            if (now >= votingEndEpochMillis) {
                votingOpen = false;
                log("Voting deadline reached — closing voting and computing results...");
                computeAndLogResults();
                break;
            }
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }

    private void computeAndLogResults() {
        long total = votes.values().stream().mapToLong(Long::longValue).sum();
        log("Total votes: " + total);
        if (total == 0) {
            log("No votes cast.");
            return;
        }
        int winnerId = -1;
        long winnerCount = -1;
        for (Map.Entry<Integer, String> e : candidates.entrySet()) {
            int id = e.getKey();
            long count = votes.getOrDefault(id, 0L);
            double pct = (count * 100.0) / total;
            log(String.format("Candidate %d - %s : %d votes (%.2f%%)", id, e.getValue(), count, pct));
            if (count > winnerCount) {
                winnerCount = count;
                winnerId = id;
            }
        }
        if (winnerId != -1) {
            log("Winner: " + candidates.get(winnerId) + " (id=" + winnerId + ") with " + winnerCount + " votes");
        }
    }

    private void sendMulticast(String message) {
        try (DatagramSocket ds = new DatagramSocket()) {
            byte[] buf = message.getBytes("UTF-8");
            InetAddress group = InetAddress.getByName(mcastAddr);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, mcastPort);
            ds.send(packet);
            log("Sent multicast: " + message);
        } catch (IOException e) {
            log("Failed to send multicast: " + e.getMessage());
        }
    }

    private void log(String s) {
        System.out.println("[VotingServer] " + s);
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String remote = socket.getRemoteSocketAddress().toString();
            log("Client connected: " + remote);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

                String line;
                String currentUser = null;
                boolean isAdmin = false;

                while ((line = in.readLine()) != null) {
                    String[] parts = line.split(" ", 2);
                    String cmd = parts[0].trim().toUpperCase(Locale.ROOT);
                    String arg = parts.length > 1 ? parts[1].trim() : "";

                    switch (cmd) {
                        case "LOGIN":
                            currentUser = arg;
                            out.println("OK");
                            sendCandidatesList(out);
                            break;
                        case "VOTE":
                            if (currentUser == null) { out.println("ERROR Not logged in"); break; }
                            handleVote(currentUser, arg, out);
                            break;
                        case "ADMIN":
                            if (arg.equals(adminPassword)) {
                                isAdmin = true;
                                out.println("OK ADMIN");
                            } else {
                                out.println("ERROR Bad admin password");
                            }
                            break;
                        case "ADD":
                            if (!isAdmin) { out.println("ERROR Not admin"); break; }
                            int id = nextCandidateId.getAndIncrement();
                            candidates.put(id, arg);
                            votes.putIfAbsent(id, 0L);
                            out.println("OK " + id);
                            log("Admin added candidate: " + id + " -> " + arg);
                            break;
                        case "REMOVE":
                            if (!isAdmin) { out.println("ERROR Not admin"); break; }
                            try {
                                int rid = Integer.parseInt(arg);
                                candidates.remove(rid);
                                votes.remove(rid);
                                out.println("OK");
                                log("Admin removed candidate: " + rid);
                            } catch (NumberFormatException nfe) { out.println("ERROR bad id"); }
                            break;
                        case "NOTIFY":
                            if (!isAdmin) { out.println("ERROR Not admin"); break; }
                            sendMulticast(arg);
                            out.println("OK");
                            break;
                        case "END":
                            if (!isAdmin) { out.println("ERROR Not admin"); break; }
                            votingOpen = false;
                            computeAndLogResults();
                            out.println("OK");
                            break;
                        case "RESULTS":
                            sendResults(out);
                            break;
                        case "QUIT":
                            out.println("BYE");
                            return;
                        default:
                            out.println("ERROR Unknown command");
                    }
                }

            } catch (IOException e) {
                log("Client handler error: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
                log("Client disconnected: " + remote);
            }
        }

        private void sendCandidatesList(PrintWriter out) {
            out.println("CAND_LIST " + candidates.size());
            for (Map.Entry<Integer, String> e : candidates.entrySet()) {
                out.println("CAND " + e.getKey() + " " + e.getValue());
            }
            out.println("END_LIST");
        }

        private void handleVote(String user, String arg, PrintWriter out) {
            if (!votingOpen) { out.println("VOTE_REJECTED Voting closed"); return; }
            if (voters.contains(user)) { out.println("VOTE_REJECTED Already voted"); return; }
            try {
                int cid = Integer.parseInt(arg);
                if (!candidates.containsKey(cid)) { out.println("VOTE_REJECTED Unknown candidate"); return; }
                votes.merge(cid, 1L, Long::sum);
                voters.add(user);
                out.println("VOTE_OK");
                log("Vote recorded: user=" + user + " -> candidate=" + cid + " (" + candidates.get(cid) + ")");
            } catch (NumberFormatException nfe) {
                out.println("VOTE_REJECTED bad id");
            }
        }

        private void sendResults(PrintWriter out) {
            long total = votes.values().stream().mapToLong(Long::longValue).sum();
            out.println("RESULTS " + total);
            for (Map.Entry<Integer, String> e : candidates.entrySet()) {
                int id = e.getKey();
                long c = votes.getOrDefault(id, 0L);
                double pct = total == 0 ? 0.0 : (c * 100.0) / total;
                out.println(String.format("RES %d %s %d %.2f", id, e.getValue(), c, pct));
            }
            out.println("END_RESULTS");
        }
    }

    public static void main(String[] args) throws Exception {
        int tcpPort = 7071;
        String mcast = "230.0.0.0";
        int mport = 4446;
        long duration = 300; // seconds
        if (args.length >= 1) tcpPort = Integer.parseInt(args[0]);
        if (args.length >= 2) mcast = args[1];
        if (args.length >= 3) mport = Integer.parseInt(args[2]);
        if (args.length >= 4) duration = Long.parseLong(args[3]);

        VotingServer server = new VotingServer(tcpPort, mcast, mport, duration);

        // add some initial candidates
        server.candidates.put(server.nextCandidateId.getAndIncrement(), "Alice");
        server.votes.putIfAbsent(1, 0L);
        server.candidates.put(server.nextCandidateId.getAndIncrement(), "Bob");
        server.votes.putIfAbsent(2, 0L);

        server.start();
    }
}

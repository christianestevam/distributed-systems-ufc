package br.ufc.ds.trabalho1.rpc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class InvestmentRpcServer {
    private final Map<String, Double> marketData = new HashMap<>();

    public InvestmentRpcServer() {
        marketData.put("PETR4", 39.10);
        marketData.put("VALE3", 68.30);
        marketData.put("ITUB4", 31.40);
        marketData.put("BBDC4", 13.00);
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 7070;
        new InvestmentRpcServer().start(port);
    }

    public void start(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("InvestmentRpcServer ouvindo em " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(() -> handleClient(socket));
                thread.start();
            }
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            int requestSize = in.readInt();
            byte[] requestPayload = new byte[requestSize];
            in.readFully(requestPayload);

            InvestmentRequest request = InvestmentRequest.fromBytes(requestPayload);
            InvestmentReply reply = process(request);
            byte[] replyPayload = reply.toBytes();

            out.writeInt(replyPayload.length);
            out.write(replyPayload);
            out.flush();
        } catch (Exception e) {
            System.err.println("Erro no cliente " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        }
    }

    private InvestmentReply process(InvestmentRequest request) {
        Double unitPrice = marketData.get(request.getTicker().toUpperCase());
        if (unitPrice == null) {
            return new InvestmentReply(false, "Ticker nao encontrado", 0, 0);
        }
        double total = unitPrice * request.getQuantity();
        String message = "Ordem " + request.getOperation().toUpperCase() + " recebida para " + request.getTicker();
        return new InvestmentReply(true, message, unitPrice, total);
    }
}

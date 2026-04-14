package br.ufc.ds.trabalho1.rpc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InvestmentRpcServer {
    private final Map<String, Double> marketData = new HashMap<>();
    private final AtomicInteger clientCounter = new AtomicInteger(0);

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
        int clientId = clientCounter.incrementAndGet();
        String clientAddr = socket.getRemoteSocketAddress().toString();
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        
        System.out.println("[" + timestamp + "] ✓ Cliente #" + clientId + " conectado de " + clientAddr);
        
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            int requestSize = in.readInt();
            byte[] requestPayload = new byte[requestSize];
            in.readFully(requestPayload);

            InvestmentRequest request = InvestmentRequest.fromBytes(requestPayload);
            
            // Log da operação
            String op = request.getOperation().toUpperCase();
            String ticker = request.getTicker().toUpperCase();
            long qty = request.getQuantity();
            System.out.println("  → Ordem: " + op + " " + qty + " x " + ticker);
            
            InvestmentReply reply = process(request);
            
            // Log do resultado
            if (reply.isSuccess()) {
                System.out.println("  ✓ Sucesso | Preço: R$ " + String.format("%.2f", reply.getUnitPrice()) + 
                                 " | Total: R$ " + String.format("%.2f", reply.getTotalValue()));
            } else {
                System.out.println("  ✗ Erro: " + reply.getMessage());
            }
            
            byte[] replyPayload = reply.toBytes();

            out.writeInt(replyPayload.length);
            out.write(replyPayload);
            out.flush();
            
            System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] ✓ Cliente #" + clientId + " desconectado");
            
        } catch (Exception e) {
            System.err.println("[ERRO] Cliente #" + clientId + " (" + clientAddr + "): " + e.getMessage());
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

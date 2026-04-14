package br.ufc.ds.trabalho1.app;

import br.ufc.ds.trabalho1.rpc.InvestmentReply;
import br.ufc.ds.trabalho1.rpc.InvestmentRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ClienteMain {
    public static void main(String[] args) throws Exception {
        String servidorIp = args.length > 0 ? args[0] : "127.0.0.1";
        int servidorPorta = args.length > 1 ? Integer.parseInt(args[1]) : 7070;

        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("[" + timestamp + "] → Conectando ao servidor " + servidorIp + ":" + servidorPorta + "...");
        
        // Teste de conexão rápida
        try {
            Socket testSocket = new Socket(servidorIp, servidorPorta);
            testSocket.close();
            System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] ✓ Conexão bem-sucedida!");
        } catch (IOException e) {
            System.err.println("[ERRO] Não foi possível conectar ao servidor: " + e.getMessage());
            return;
        }
        
        System.out.println("");
        System.out.println("Cliente de Investimentos B3 (simulado)");
        System.out.println("Servidor: " + servidorIp + ":" + servidorPorta);
        System.out.println("Digite: COMPRA PETR4 10 | VENDA VALE3 5 | SAIR");
        System.out.println("");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine();
                if (line == null || line.isBlank()) {
                    continue;
                }
                if ("SAIR".equalsIgnoreCase(line.trim())) {
                    System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] Desconectando...");
                    break;
                }

                String[] parts = line.trim().split("\\s+");
                if (parts.length != 3) {
                    System.out.println("Formato invalido. Use: OPERACAO TICKER QUANTIDADE");
                    continue;
                }

                String operation = parts[0].toUpperCase();
                String ticker = parts[1].toUpperCase();
                long quantity;
                try {
                    quantity = Long.parseLong(parts[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Quantidade deve ser numero inteiro.");
                    continue;
                }

                InvestmentReply reply = sendRequest(servidorIp, servidorPorta, operation, ticker, quantity);
                System.out.println(reply);
            }
        }
    }

    private static InvestmentReply sendRequest(String host, int port, String operation, String ticker, long quantity) throws IOException {
        InvestmentRequest request = new InvestmentRequest(operation, ticker, quantity);
        byte[] requestPayload = request.toBytes();

        try {
            Socket socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), 5000); // Timeout de 5 segundos
            
            try (socket;
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream())) {

                out.writeInt(requestPayload.length);
                out.write(requestPayload);
                out.flush();

                int replySize = in.readInt();
                byte[] replyPayload = new byte[replySize];
                in.readFully(replyPayload);
                return InvestmentReply.fromBytes(replyPayload);
            }
        } catch (java.net.ConnectException e) {
            System.err.println("[ERRO] Connection refused - Servidor não está rodando em " + host + ":" + port);
            throw e;
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("[ERRO] Connection timeout - Servidor não responde (5s)");
            throw e;
        } catch (IOException e) {
            System.err.println("[ERRO] " + e.getMessage());
            throw e;
        }
    }
}
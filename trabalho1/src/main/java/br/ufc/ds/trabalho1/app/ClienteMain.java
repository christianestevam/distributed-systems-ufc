package br.ufc.ds.trabalho1.app;

import br.ufc.ds.trabalho1.rpc.InvestmentReply;
import br.ufc.ds.trabalho1.rpc.InvestmentRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClienteMain {
    public static void main(String[] args) throws Exception {
        String servidorIp = args.length > 0 ? args[0] : "127.0.0.1";
        int servidorPorta = args.length > 1 ? Integer.parseInt(args[1]) : 7070;

        System.out.println("Cliente de Investimentos conectado com B3 (simulado)");
        System.out.println("Servidor: " + servidorIp + ":" + servidorPorta);
        System.out.println("Digite: COMPRA PETR4 10 | VENDA VALE3 5 | SAIR");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine();
                if (line == null || line.isBlank()) {
                    continue;
                }
                if ("SAIR".equalsIgnoreCase(line.trim())) {
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

        try (Socket socket = new Socket(host, port);
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
    }
}
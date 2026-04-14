package br.ufc.ds.trabalho1.rpc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class InvestmentRpcClient {
    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 7070;
        String operation = args.length > 2 ? args[2] : "COMPRA";
        String ticker = args.length > 3 ? args[3] : "PETR4";
        long quantity = args.length > 4 ? Long.parseLong(args[4]) : 10;

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

            InvestmentReply reply = InvestmentReply.fromBytes(replyPayload);
            System.out.println(reply);
        }
    }
}

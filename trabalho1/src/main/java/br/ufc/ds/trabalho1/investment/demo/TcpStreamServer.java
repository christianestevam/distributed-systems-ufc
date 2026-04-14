package br.ufc.ds.trabalho1.investment.demo;

import br.ufc.ds.trabalho1.investment.model.OrdemInvestimento;
import br.ufc.ds.trabalho1.investment.stream.OrdemInvestimentoInputStream;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class TcpStreamServer {
    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 9090;
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("TcpStreamServer ouvindo em " + porta);
            while (true) {
                Socket client = serverSocket.accept();
                try (client) {
                    try (OrdemInvestimentoInputStream in = new OrdemInvestimentoInputStream(client.getInputStream())) {
                    OrdemInvestimento[] ordens = in.readOrdens();
                    System.out.println("Recebido de " + client.getRemoteSocketAddress() + ": " + Arrays.toString(ordens));
                    }
                }
            }
        }
    }
}

package br.ufc.ds.trabalho1.investment.demo;

import br.ufc.ds.trabalho1.investment.model.OrdemInvestimento;
import br.ufc.ds.trabalho1.investment.stream.OrdemInvestimentoOutputStream;

import java.io.IOException;
import java.net.Socket;

public class TcpStreamClient {
    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9090;

        OrdemInvestimento[] ordens = {
            new OrdemInvestimento("O-100", "BBDC4", "COMPRA", 120, 12.70),
            new OrdemInvestimento("O-101", "ITUB4", "VENDA", 80, 31.10)
        };

        try (Socket socket = new Socket(host, port);
             OrdemInvestimentoOutputStream out = new OrdemInvestimentoOutputStream(ordens, ordens.length, socket.getOutputStream())) {
            out.writeOrdens();
            System.out.println("Ordens enviadas para " + host + ":" + port);
        }
    }
}

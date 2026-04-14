package br.ufc.ds.trabalho1.app;

import br.ufc.ds.trabalho1.rpc.InvestmentRpcServer;

public class ServidorMain {
    public static void main(String[] args) throws Exception {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 7070;

        InvestmentRpcServer server = new InvestmentRpcServer();
        server.start(tcpPort);
    }
}
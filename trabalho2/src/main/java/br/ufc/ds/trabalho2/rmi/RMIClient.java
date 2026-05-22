package br.ufc.ds.trabalho2.rmi;

import br.ufc.ds.trabalho2.model.*;
import java.io.*;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Cliente RMI que usa protocolo UDP requisição-resposta.
 * Envia requisições para o servidor remoto e recebe respostas serializadas em Protocol Buffers.
 */
public class RMIClient {
    private RMICommunication communication;
    private RemoteObjectRef serviceRef;

    public RMIClient(String host, int port) throws Exception {
        this.communication = new RMICommunication(0); // Port 0 = any available port
        this.serviceRef = new RemoteObjectRef("investidor_service", InetAddress.getByName(host), port);
        System.out.println("[RMIClient] Conectado ao servidor em " + host + ":" + port);
    }

    /**
     * Chama método remoto criarInvestidor.
     * Demonstra passagem por VALOR (argumentos primitivos).
     */
    public Investidor criarInvestidor(String investidorId, String nome, String cpf, String email, String telefone) throws Exception {
        // Serializa argumentos
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(investidorId);
        dos.writeUTF(nome);
        dos.writeUTF(cpf);
        dos.writeUTF(email);
        dos.writeUTF(telefone);
        dos.flush();

        // Envia requisição e recebe resposta
        byte[] responseBytes = communication.doOperation(serviceRef, "criarInvestidor", baos.toByteArray());

        // Deserializa resposta com Protocol Buffers
        if (responseBytes.length == 0) {
            return null;
        }
        br.ufc.ds.trabalho2.rmi.pb.Investidor pbResult = br.ufc.ds.trabalho2.rmi.pb.Investidor.parseFrom(responseBytes);
        return ProtobufSerializer.deserializeInvestidor(pbResult);
    }

    /**
     * Chama método remoto obterInvestidor.
     * Demonstra passagem por REFERÊNCIA no request (RemoteObjectRef) e
     * passagem por VALOR na resposta (Investidor serializado).
     */
    public Investidor obterInvestidor(String investidorId) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(investidorId);
        dos.flush();

        byte[] responseBytes = communication.doOperation(serviceRef, "obterInvestidor", baos.toByteArray());

        if (responseBytes.length == 0) {
            return null;
        }
        br.ufc.ds.trabalho2.rmi.pb.Investidor pbResult = br.ufc.ds.trabalho2.rmi.pb.Investidor.parseFrom(responseBytes);
        return ProtobufSerializer.deserializeInvestidor(pbResult);
    }

    public OrdemInvestimento criarOrdem(String ordemId, String tipo, String ticker, long quantidade, double precoUnitario) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(ordemId);
        dos.writeUTF(tipo);
        dos.writeUTF(ticker);
        dos.writeLong(quantidade);
        dos.writeDouble(precoUnitario);
        dos.flush();

        byte[] responseBytes = communication.doOperation(serviceRef, "criarOrdem", baos.toByteArray());

        if (responseBytes.length == 0) {
            return null;
        }
        br.ufc.ds.trabalho2.rmi.pb.OrdemInvestimento pbResult = br.ufc.ds.trabalho2.rmi.pb.OrdemInvestimento.parseFrom(responseBytes);
        return ProtobufSerializer.deserializeOrdem(pbResult);
    }

    public OrdemInvestimento[] obterOrdensDoInvestidor(String investidorId) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(investidorId);
        dos.flush();

        byte[] responseBytes = communication.doOperation(serviceRef, "obterOrdensDoInvestidor", baos.toByteArray());

        if (responseBytes.length == 0) {
            return new OrdemInvestimento[0];
        }
        br.ufc.ds.trabalho2.rmi.pb.OrdensListaWrapper pbList = br.ufc.ds.trabalho2.rmi.pb.OrdensListaWrapper.parseFrom(responseBytes);
        
        OrdemInvestimento[] resultado = new OrdemInvestimento[pbList.getOrdensCount()];
        for (int i = 0; i < pbList.getOrdensCount(); i++) {
            resultado[i] = ProtobufSerializer.deserializeOrdem(pbList.getOrdens(i));
        }
        return resultado;
    }

    public double adicionarSaldoCarteira(String investidorId, double valor) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(investidorId);
        dos.writeDouble(valor);
        dos.flush();

        byte[] responseBytes = communication.doOperation(serviceRef, "adicionarSaldoCarteira", baos.toByteArray());

        ByteArrayInputStream bais = new ByteArrayInputStream(responseBytes);
        DataInputStream dis = new DataInputStream(bais);
        return dis.readDouble();
    }

    public Ativo obterAtivo(String ticker) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(ticker);
        dos.flush();

        byte[] responseBytes = communication.doOperation(serviceRef, "obterAtivo", baos.toByteArray());

        if (responseBytes.length == 0) {
            return null;
        }
        br.ufc.ds.trabalho2.rmi.pb.Ativo pbResult = br.ufc.ds.trabalho2.rmi.pb.Ativo.parseFrom(responseBytes);
        return ProtobufSerializer.deserializeAtivo(pbResult);
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        RMIClient client = new RMIClient(host, port);
        
        System.out.println("\n========== Cliente RMI com UDP ==========");
        System.out.println("Digite o numero da opcao e forneca os dados conforme solicitado.");
        System.out.println("Para criar ordens use um ticker valido, ex: PETR4, VALE3, ITUB4, LTN.");
        System.out.println("=========================================\n");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Menu RMI Cliente ===");
            System.out.println("1) criarInvestidor - criar novo investidor com dados pessoais");
            System.out.println("2) obterInvestidor - recuperar investidor existente pelo ID");
            System.out.println("3) criarOrdem - criar ordem de investimento para um ticker existente");
            System.out.println("4) obterOrdensDoInvestidor - listar ordens associadas ao investidor");
            System.out.println("5) adicionarSaldoCarteira - adicionar saldo na carteira de um investidor");
            System.out.println("6) obterAtivo - consultar detalhes de um ativo pelo ticker");
            System.out.println("7) sair");
            System.out.print("Escolha uma opcao (1-7): ");

            String opt = scanner.nextLine().trim();
            try {
                switch (opt) {
                    case "1": {
                        System.out.println("Criar um investidor exige um ID unico e dados pessoais.");
                        System.out.print("investidorId (ex: inv1): ");
                        String id = scanner.nextLine().trim();
                        System.out.print("nome completo: ");
                        String nome = scanner.nextLine().trim();
                        System.out.print("cpf (ex: 000.000.000-00): ");
                        String cpf = scanner.nextLine().trim();
                        System.out.print("email: ");
                        String email = scanner.nextLine().trim();
                        System.out.print("telefone (ex: 99999-0000): ");
                        String tel = scanner.nextLine().trim();
                        Investidor inv = client.criarInvestidor(id, nome, cpf, email, tel);
                        System.out.println("Criado: " + inv);
                        break;
                    }
                    case "2": {
                        System.out.println("Recuperar um investidor existente pelo ID usado ao criar.");
                        System.out.print("investidorId (ex: inv1): ");
                        String id = scanner.nextLine().trim();
                        Investidor inv = client.obterInvestidor(id);
                        System.out.println("Resultado: " + inv);
                        break;
                    }
                    case "3": {
                        System.out.println("Use tipo COMPRA ou VENDA. Tickers validos: PETR4, VALE3, ITUB4, LTN.");
                        System.out.print("ordemId (ex: ORD123): ");
                        String ordemId = scanner.nextLine().trim();
                        System.out.print("tipo (COMPRA/VENDA): ");
                        String tipo = scanner.nextLine().trim();
                        System.out.print("ticker (PETR4, VALE3, ITUB4, LTN): ");
                        String ticker = scanner.nextLine().trim();
                        System.out.print("quantidade (numero inteiro): ");
                        long q = Long.parseLong(scanner.nextLine().trim());
                        System.out.print("precoUnitario (numero decimal): ");
                        double p = Double.parseDouble(scanner.nextLine().trim());
                        OrdemInvestimento ordem = client.criarOrdem(ordemId, tipo, ticker, q, p);
                        System.out.println("Ordem criada: " + ordem);
                        break;
                    }
                    case "4": {
                        System.out.println("Listar todas as ordens de um investidor especifico.");
                        System.out.print("investidorId (ex: inv1): ");
                        String id = scanner.nextLine().trim();
                        OrdemInvestimento[] ords = client.obterOrdensDoInvestidor(id);
                        System.out.println("Ordens encontradas: " + (ords == null ? 0 : ords.length));
                        if (ords != null) {
                            for (OrdemInvestimento o : ords) System.out.println(o);
                        }
                        break;
                    }
                    case "5": {
                        System.out.println("Adicionar saldo a carteira de um investidor existente.");
                        System.out.print("investidorId (ex: inv1): ");
                        String id = scanner.nextLine().trim();
                        System.out.print("valor a adicionar (ex: 1000.50): ");
                        double val = Double.parseDouble(scanner.nextLine().trim());
                        double novo = client.adicionarSaldoCarteira(id, val);
                        System.out.println("Novo saldo: " + novo);
                        break;
                    }
                    case "6": {
                        System.out.println("TICKERS validos atualmente: PETR4, VALE3, ITUB4, LTN");
                        System.out.print("ticker para consulta: ");
                        String t = scanner.nextLine().trim();
                        Ativo a = client.obterAtivo(t);
                        System.out.println("Ativo: " + a);
                        break;
                    }
                    case "7": {
                        System.out.println("Saindo...");
                        scanner.close();
                        return;
                    }
                    default:
                        System.out.println("Opcao invalida");
                }
            } catch (Exception e) {
                System.err.println("Erro na chamada remota: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

package br.ufc.ds.trabalho2.rmi;

import br.ufc.ds.trabalho2.app.InvestidorServiceImpl;
import br.ufc.ds.trabalho2.model.*;
import java.io.*;
import java.net.InetAddress;

/**
 * Servidor RMI que implementa o protocolo requisição-resposta com UDP.
 * Aguarda requisições de clientes, executa métodos remotos e envia respostas.
 */
public class RMIServer {
    private final String host;
    private final int port;
    private RMICommunication communication;
    private InvestidorServiceImpl service;

    public RMIServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            // Inicializa comunicação UDP
            communication = new RMICommunication(port);
            service = new InvestidorServiceImpl();

            System.out.println("[RMIServer] Servidor RMI iniciado em UDP port " + port);
            System.out.println("[RMIServer] Aguardando requisições...");

            // Loop infinito aguardando requisições
            while (true) {
                try {
                    // Recebe requisição do cliente
                    RMIRequest request = communication.getRequest();
                    System.out.println("[RMIServer] Requisição recebida: " + request.getMethodId() + 
                                      " (requestId=" + request.getRequestId() + ")");

                    // Executa método remoto
                    byte[] resultBytes = executeRemoteMethod(request);

                    // Envia resposta
                    communication.sendReply(resultBytes, InetAddress.getByName("localhost"), 
                                          request.getRequestId());
                    System.out.println("[RMIServer] Resposta enviada para requestId=" + request.getRequestId());

                } catch (IOException e) {
                    System.err.println("[RMIServer] Erro ao processar requisição: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("[RMIServer] Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (communication != null) {
                communication.close();
            }
        }
    }

    /**
     * Executa o método remoto especificado na requisição.
     * Deserializa argumentos com DataInputStream, executa método e serializa resultado com Protocol Buffers.
     */
    private byte[] executeRemoteMethod(RMIRequest request) throws IOException {
        String methodId = request.getMethodId();
        byte[] arguments = request.getArguments();

        try {
            switch (methodId) {
                case "criarInvestidor":
                    return handleCriarInvestidor(arguments);
                case "obterInvestidor":
                    return handleObterInvestidor(arguments);
                case "criarOrdem":
                    return handleCriarOrdem(arguments);
                case "obterOrdensDoInvestidor":
                    return handleObterOrdensDoInvestidor(arguments);
                case "adicionarSaldoCarteira":
                    return handleAdicionarSaldoCarteira(arguments);
                case "obterAtivo":
                    return handleObterAtivo(arguments);
                default:
                    throw new IOException("Método não encontrado: " + methodId);
            }
        } catch (Exception e) {
            System.err.println("[RMIServer] Erro ao executar método: " + e.getMessage());
            throw new IOException("Erro ao executar método " + methodId + ": " + e.getMessage());
        }
    }

    private byte[] handleCriarInvestidor(byte[] arguments) throws IOException {
        // Deserializa argumentos: [investidorId, nome, cpf, email, telefone]
        ByteArrayInputStream bais = new ByteArrayInputStream(arguments);
        DataInputStream dis = new DataInputStream(bais);
        
        String investidorId = dis.readUTF();
        String nome = dis.readUTF();
        String cpf = dis.readUTF();
        String email = dis.readUTF();
        String telefone = dis.readUTF();

        // Executa método
        Investidor resultado = service.criarInvestidor(investidorId, nome, cpf, email, telefone);

        // Serializa resultado com Protocol Buffers
        br.ufc.ds.trabalho2.rmi.pb.Investidor pbResult = ProtobufSerializer.serializeInvestidor(resultado);
        return pbResult.toByteArray();
    }

    private byte[] handleObterInvestidor(byte[] arguments) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(arguments);
        DataInputStream dis = new DataInputStream(bais);
        String investidorId = dis.readUTF();

        Investidor resultado = service.obterInvestidor(investidorId);
        if (resultado == null) {
            return new byte[0]; // Retorna vazio se não encontrado
        }

        br.ufc.ds.trabalho2.rmi.pb.Investidor pbResult = ProtobufSerializer.serializeInvestidor(resultado);
        return pbResult.toByteArray();
    }

    private byte[] handleCriarOrdem(byte[] arguments) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(arguments);
        DataInputStream dis = new DataInputStream(bais);
        
        String ordemId = dis.readUTF();
        String tipo = dis.readUTF();
        String ticker = dis.readUTF();
        long quantidade = dis.readLong();
        double precoUnitario = dis.readDouble();

        OrdemInvestimento resultado = service.criarOrdem(ordemId, tipo, ticker, quantidade, precoUnitario);
        if (resultado == null) {
            return new byte[0];
        }

        br.ufc.ds.trabalho2.rmi.pb.OrdemInvestimento pbResult = ProtobufSerializer.serializeOrdem(resultado);
        return pbResult.toByteArray();
    }

    private byte[] handleObterOrdensDoInvestidor(byte[] arguments) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(arguments);
        DataInputStream dis = new DataInputStream(bais);
        String investidorId = dis.readUTF();

        OrdemInvestimento[] resultado = service.obterOrdensDoInvestidor(investidorId);

        // Serializa array de ordens usando wrapper
        br.ufc.ds.trabalho2.rmi.pb.OrdensListaWrapper.Builder builder = br.ufc.ds.trabalho2.rmi.pb.OrdensListaWrapper.newBuilder();
        for (OrdemInvestimento ordem : resultado) {
            builder.addOrdens(ProtobufSerializer.serializeOrdem(ordem));
        }
        return builder.build().toByteArray();
    }

    private byte[] handleAdicionarSaldoCarteira(byte[] arguments) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(arguments);
        DataInputStream dis = new DataInputStream(bais);
        String investidorId = dis.readUTF();
        double valor = dis.readDouble();

        double novoSaldo = service.adicionarSaldoCarteira(investidorId, valor);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeDouble(novoSaldo);
        dos.flush();
        return baos.toByteArray();
    }

    private byte[] handleObterAtivo(byte[] arguments) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(arguments);
        DataInputStream dis = new DataInputStream(bais);
        String ticker = dis.readUTF();

        Ativo resultado = service.obterAtivo(ticker);
        if (resultado == null) {
            return new byte[0];
        }

        br.ufc.ds.trabalho2.rmi.pb.Ativo pbResult = ProtobufSerializer.serializeAtivo(resultado);
        return pbResult.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        String host;
        int port;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } else if (args.length == 1) {
            host = InetAddress.getLocalHost().getHostAddress();
            port = Integer.parseInt(args[0]);
        } else {
            host = "localhost";
            port = 5000;
        }

        System.out.println("[RMIServer] Iniciando servidor RMI com UDP");
        System.out.println("[RMIServer] Host: " + host + ", Port: " + port);

        RMIServer server = new RMIServer(host, port);
        server.start();
    }
}


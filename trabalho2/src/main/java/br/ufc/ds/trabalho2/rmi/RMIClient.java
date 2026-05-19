package br.ufc.ds.trabalho2.rmi;

import java.io.*;
import java.net.InetAddress;

/**
 * Cliente RMI que faz chamadas remotas para o servidor.
 * Utiliza serialização Java nativa para objetos.
 */
public class RMIClient {
    private RMICommunication communication;
    private RemoteObjectRef remoteRef;

    public RMIClient(String host, int port, String objectName) throws Exception {
        this.communication = new RMICommunication(0);
        this.remoteRef = new RemoteObjectRef(
                objectName,
                InetAddress.getByName(host),
                port
        );
    }

    /**
     * Invoca um método remoto usando doOperation.
     * Serialização via ObjectInputStream/ObjectOutputStream.
     */
    public Object invokeRemoteMethod(String methodName, Object[] args) throws Exception {
        byte[] argumentsData = serializeArguments(args);
        byte[] resultData = communication.doOperation(remoteRef, methodName, argumentsData);
        return deserializeResult(resultData);
    }

    private byte[] serializeArguments(Object[] args) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(args);
        oos.flush();
        return baos.toByteArray();
    }

    private Object deserializeResult(byte[] data) throws IOException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro: " + e.getMessage());
        }
    }

    public void close() {
        communication.close();
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9999;
        
        RMIClient client = new RMIClient(host, port, "investidor_service");
        
        try {
            System.out.println("[RMIClient] Conectando ao servidor em " + host + ":" + port);
        } finally {
            client.close();
        }
    }
}

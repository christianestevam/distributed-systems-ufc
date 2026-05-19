package br.ufc.ds.trabalho2.rmi;

import java.io.*;
import java.net.*;

/**
 * Implementação do protocolo requisição-resposta RMI usando UDP.
 * Métodos: doOperation, getRequest, sendReply.
 */
public class RMICommunication {

    private DatagramSocket socket;
    private int serverPort;

    public RMICommunication(int serverPort) throws SocketException {
        this.serverPort = serverPort;
        this.socket = new DatagramSocket(serverPort);
    }

    /**
     * Envia uma requisição e aguarda resposta.
     * doOperation(RemoteObjectRef, methodId, arguments)
     */
    public byte[] doOperation(RemoteObjectRef remoteRef, String methodId, byte[] arguments) 
            throws IOException {
        
        RMIRequest request = new RMIRequest();
        request.setObjectReference(remoteRef.getObjectReference());
        request.setMethodId(methodId);
        request.setArguments(arguments);
        request.setRequestId((int) (System.nanoTime() % 100000));

        byte[] requestBytes = serializeRequest(request);

        DatagramPacket packet = new DatagramPacket(
                requestBytes, 
                requestBytes.length,
                remoteRef.getHostAddress(),
                remoteRef.getPort()
        );
        socket.send(packet);

        byte[] receiveBuffer = new byte[65536];
        DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        socket.receive(responsePacket);

        RMIReply reply = deserializeReply(responsePacket.getData(), responsePacket.getLength());
        
        if (!reply.isSuccess()) {
            throw new IOException("RMI Exception: " + reply.getException());
        }

        return reply.getResult();
    }

    /**
     * Obtém uma requisição de um cliente.
     * getRequest()
     */
    public RMIRequest getRequest() throws IOException {
        byte[] buffer = new byte[65536];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        
        RMIRequest request = deserializeRequest(packet.getData(), packet.getLength());
        request.setRequestId(packet.getPort());
        
        return request;
    }

    /**
     * Envia a resposta para o cliente.
     * sendReply(replyData, clientHost, clientPort)
     */
    public void sendReply(byte[] replyData, InetAddress clientHost, int clientPort) throws IOException {
        RMIReply reply = new RMIReply();
        reply.setResult(replyData);
        
        byte[] replyBytes = serializeReply(reply);
        DatagramPacket packet = new DatagramPacket(
                replyBytes,
                replyBytes.length,
                clientHost,
                clientPort
        );
        socket.send(packet);
    }

    public void sendReplyWithException(String exceptionMessage, InetAddress clientHost, int clientPort) 
            throws IOException {
        RMIReply reply = new RMIReply();
        reply.setException(exceptionMessage);
        
        byte[] replyBytes = serializeReply(reply);
        
        DatagramPacket packet = new DatagramPacket(
                replyBytes,
                replyBytes.length,
                clientHost,
                clientPort
        );
        socket.send(packet);
    }

    private byte[] serializeRequest(RMIRequest request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        oos.flush();
        return baos.toByteArray();
    }

    private RMIRequest deserializeRequest(byte[] data, int length) throws IOException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data, 0, length);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (RMIRequest) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Deserialization error: " + e.getMessage());
        }
    }

    private byte[] serializeReply(RMIReply reply) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(reply);
        oos.flush();
        return baos.toByteArray();
    }

    private RMIReply deserializeReply(byte[] data, int length) throws IOException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data, 0, length);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (RMIReply) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Deserialization error: " + e.getMessage());
        }
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}

package br.ufc.ds.trabalho2.rmi;

import java.io.*;
import java.lang.reflect.Method;

/**
 * Servidor RMI que aguarda requisições e executa métodos remotos.
 */
public class RMIServer {
    private RMICommunication communication;
    private RemoteObjectManager objectManager;
    private int port;

    public RMIServer(int port) throws Exception {
        this.port = port;
        this.communication = new RMICommunication(port);
        this.objectManager = new RemoteObjectManager();
    }

    public void registerRemoteObject(String objectName, Object object) {
        objectManager.registerObject(objectName, object);
        System.out.println("[RMIServer] Objeto remoto registrado: " + objectName);
    }

    public void start() {
        System.out.println("[RMIServer] Servidor RMI aguardando requisições na porta " + port + "...");
        
        while (true) {
            try {
                RMIRequest request = communication.getRequest();
                System.out.println("[RMIServer] Requisição: " + request);
                
                new Thread(() -> processRequest(request)).start();
                
            } catch (Exception e) {
                System.err.println("[RMIServer] Erro: " + e.getMessage());
            }
        }
    }

    private void processRequest(RMIRequest request) {
        try {
            Object remoteObject = objectManager.getObject(request.getObjectReference());
            
            if (remoteObject == null) {
                System.err.println("[RMIServer] Objeto nao encontrado: " + request.getObjectReference());
                return;
            }

            Object[] methodArgs = deserializeArguments(request.getArguments());
            Method method = findMethod(remoteObject.getClass(), request.getMethodId(), methodArgs);
            
            if (method == null) {
                System.err.println("[RMIServer] Metodo nao encontrado: " + request.getMethodId());
                return;
            }

            Object result = method.invoke(remoteObject, methodArgs);
            byte[] resultBytes = serializeResult(result);
            
            System.out.println("[RMIServer] Metodo executado: " + request.getMethodId());
            
        } catch (Exception e) {
            System.err.println("[RMIServer] Erro ao processar: " + e.getMessage());
        }
    }

    private Method findMethod(Class<?> clazz, String methodName, Object[] args) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == args.length) {
                    boolean compatible = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (args[i] != null && !paramTypes[i].isAssignableFrom(args[i].getClass())) {
                            compatible = false;
                            break;
                        }
                    }
                    if (compatible) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private Object[] deserializeArguments(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return new Object[0];
        }
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Object[]) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao desserializar: " + e.getMessage());
        }
    }

    private byte[] serializeResult(Object result) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(result);
        oos.flush();
        return baos.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9999;
        RMIServer server = new RMIServer(port);
        server.start();
    }
}

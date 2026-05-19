package main.java.br.ufc.ds.trabalho2.rmi;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Classe que representa uma referência remota a um objeto.
 */
public class RemoteObjectRef implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String objectReference;  // nome do objeto remoto
    private InetAddress hostAddress;
    private int port;
    private String serviceVersion;

    public RemoteObjectRef(String objectReference, InetAddress hostAddress, int port) {
        this.objectReference = objectReference;
        this.hostAddress = hostAddress;
        this.port = port;
        this.serviceVersion = "1.0";
    }

    public String getObjectReference() {
        return objectReference;
    }

    public void setObjectReference(String objectReference) {
        this.objectReference = objectReference;
    }

    public InetAddress getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public String toString() {
        return "RemoteObjectRef{" +
                "objectReference='" + objectReference + '\'' +
                ", hostAddress=" + hostAddress +
                ", port=" + port +
                '}';
    }
}

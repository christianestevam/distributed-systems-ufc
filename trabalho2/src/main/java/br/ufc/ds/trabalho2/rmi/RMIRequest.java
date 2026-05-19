package main.java.br.ufc.ds.trabalho2.rmi;

import java.io.Serializable;

/**
 * Requisição RMI: messageType=0, requestId, objectReference, methodId, arguments.
 */
public class RMIRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int messageType;  // 0 = Request
    private int requestId;
    private String objectReference;
    private String methodId;
    private byte[] arguments;

    public RMIRequest() {
        this.messageType = 0;
    }

    public RMIRequest(int requestId, String objectReference, String methodId, byte[] arguments) {
        this();
        this.requestId = requestId;
        this.objectReference = objectReference;
        this.methodId = methodId;
        this.arguments = arguments;
    }

    public int getMessageType() {
        return messageType;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getObjectReference() {
        return objectReference;
    }

    public void setObjectReference(String objectReference) {
        this.objectReference = objectReference;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public byte[] getArguments() {
        return arguments;
    }

    public void setArguments(byte[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "RMIRequest{" +
                "requestId=" + requestId +
                ", objectReference='" + objectReference + '\'' +
                ", methodId='" + methodId + '\'' +
                '}';
    }
}

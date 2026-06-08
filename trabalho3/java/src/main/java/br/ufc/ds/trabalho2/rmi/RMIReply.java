package br.ufc.ds.trabalho2.rmi;

import java.io.Serializable;

/**
 * Resposta RMI: messageType=1, requestId, result, exception.
 */
public class RMIReply implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int messageType;  // 1 = Reply
    private int requestId;
    private byte[] result;
    private String exception;
    private boolean success;

    public RMIReply() {
        this.messageType = 1;
        this.success = true;
    }

    public RMIReply(int requestId, byte[] result) {
        this();
        this.requestId = requestId;
        this.result = result;
        this.success = true;
    }

    public RMIReply(int requestId, String exception) {
        this();
        this.requestId = requestId;
        this.exception = exception;
        this.success = false;
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

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
        this.success = true;
        this.exception = null;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "RMIReply{" +
                "requestId=" + requestId +
                ", success=" + success +
                '}';
    }
}

package br.ufc.ds.trabalho1.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InvestmentReply {
    private final boolean success;
    private final String message;
    private final double unitPrice;
    private final double totalValue;

    public InvestmentReply(boolean success, String message, double unitPrice, double totalValue) {
        this.success = success;
        this.message = message;
        this.unitPrice = unitPrice;
        this.totalValue = totalValue;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeBoolean(success);
        out.writeUTF(message);
        out.writeDouble(unitPrice);
        out.writeDouble(totalValue);
        out.flush();
        return baos.toByteArray();
    }

    public static InvestmentReply fromBytes(byte[] payload) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
        boolean success = in.readBoolean();
        String message = in.readUTF();
        double unitPrice = in.readDouble();
        double totalValue = in.readDouble();
        return new InvestmentReply(success, message, unitPrice, totalValue);
    }

    @Override
    public String toString() {
        return "InvestmentReply{" +
            "success=" + success +
            ", message='" + message + '\'' +
            ", unitPrice=" + unitPrice +
            ", totalValue=" + totalValue +
            '}';
    }
}

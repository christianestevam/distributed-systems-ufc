package br.ufc.ds.trabalho1.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InvestmentRequest {
    private final String operation;
    private final String ticker;
    private final long quantity;

    public InvestmentRequest(String operation, String ticker, long quantity) {
        this.operation = operation;
        this.ticker = ticker;
        this.quantity = quantity;
    }

    public String getOperation() {
        return operation;
    }

    public String getTicker() {
        return ticker;
    }

    public long getQuantity() {
        return quantity;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeUTF(operation);
        out.writeUTF(ticker);
        out.writeLong(quantity);
        out.flush();
        return baos.toByteArray();
    }

    public static InvestmentRequest fromBytes(byte[] payload) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
        String op = in.readUTF();
        String ticker = in.readUTF();
        long qty = in.readLong();
        return new InvestmentRequest(op, ticker, qty);
    }
}

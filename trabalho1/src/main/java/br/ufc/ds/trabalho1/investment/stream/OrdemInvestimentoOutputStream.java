package br.ufc.ds.trabalho1.investment.stream;

import br.ufc.ds.trabalho1.investment.model.OrdemInvestimento;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OrdemInvestimentoOutputStream extends OutputStream {
    private final OrdemInvestimento[] ordens;
    private final int quantidade;
    private final OutputStream destino;

    public OrdemInvestimentoOutputStream(OrdemInvestimento[] ordens, int quantidade, OutputStream destino) {
        this.ordens = ordens;
        this.quantidade = quantidade;
        this.destino = destino;
    }

    public void writeOrdens() throws IOException {
        DataOutputStream out = new DataOutputStream(destino);
        int total = Math.min(quantidade, ordens.length);
        out.writeInt(total);

        for (int i = 0; i < total; i++) {
            byte[] payload = serializarOrdem(ordens[i]);
            out.writeInt(payload.length);
            out.write(payload);
        }
        out.flush();
    }

    private byte[] serializarOrdem(OrdemInvestimento ordem) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(baos);

        data.writeUTF(ordem.getOrdemId());
        data.writeUTF(ordem.getTicker());
        data.writeUTF(ordem.getTipo());
        data.writeLong(ordem.getQuantidade());
        data.writeDouble(ordem.getPrecoUnitario());
        data.flush();
        return baos.toByteArray();
    }

    @Override
    public void write(int b) throws IOException {
        destino.write(b);
    }

    @Override
    public void close() throws IOException {
        destino.close();
    }
}

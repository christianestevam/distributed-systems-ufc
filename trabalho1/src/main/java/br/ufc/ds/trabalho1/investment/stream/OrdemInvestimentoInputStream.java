package br.ufc.ds.trabalho1.investment.stream;

import br.ufc.ds.trabalho1.investment.model.OrdemInvestimento;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OrdemInvestimentoInputStream extends InputStream {
    private final InputStream origem;

    public OrdemInvestimentoInputStream(InputStream origem) {
        this.origem = origem;
    }

    public OrdemInvestimento[] readOrdens() throws IOException {
        DataInputStream in = new DataInputStream(origem);
        int total = in.readInt();
        OrdemInvestimento[] resultado = new OrdemInvestimento[total];

        for (int i = 0; i < total; i++) {
            int bytesDaOrdem = in.readInt();
            byte[] payload = new byte[bytesDaOrdem];
            in.readFully(payload);
            resultado[i] = desserializarOrdem(payload);
        }
        return resultado;
    }

    private OrdemInvestimento desserializarOrdem(byte[] payload) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
        String ordemId = in.readUTF();
        String ticker = in.readUTF();
        String tipo = in.readUTF();
        long quantidade = in.readLong();
        double precoUnitario = in.readDouble();
        return new OrdemInvestimento(ordemId, ticker, tipo, quantidade, precoUnitario);
    }

    @Override
    public int read() throws IOException {
        return origem.read();
    }

    @Override
    public void close() throws IOException {
        origem.close();
    }
}

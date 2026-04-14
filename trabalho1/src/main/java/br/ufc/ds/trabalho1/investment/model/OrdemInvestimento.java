package br.ufc.ds.trabalho1.investment.model;

public class OrdemInvestimento {
    private final String ordemId;
    private final String ticker;
    private final String tipo;
    private final long quantidade;
    private final double precoUnitario;

    public OrdemInvestimento(String ordemId, String ticker, String tipo, long quantidade, double precoUnitario) {
        this.ordemId = ordemId;
        this.ticker = ticker;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public String getOrdemId() {
        return ordemId;
    }

    public String getTicker() {
        return ticker;
    }

    public String getTipo() {
        return tipo;
    }

    public long getQuantidade() {
        return quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    @Override
    public String toString() {
        return "OrdemInvestimento{" +
            "ordemId='" + ordemId + '\'' +
            ", ticker='" + ticker + '\'' +
            ", tipo='" + tipo + '\'' +
            ", quantidade=" + quantidade +
            ", precoUnitario=" + precoUnitario +
            '}';
    }
}

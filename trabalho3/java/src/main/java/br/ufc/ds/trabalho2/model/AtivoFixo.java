package br.ufc.ds.trabalho2.model;

/**
 * Extensão de Ativo para renda fixa (segunda extensão "é-um").
 */
public class AtivoFixo extends Ativo {
    private static final long serialVersionUID = 1L;
    
    private double taxaJuros;
    private String dataVencimento;

    public AtivoFixo(String ticker, double precoAtual, String descricao, double taxaJuros, String dataVencimento) {
        super(ticker, precoAtual, descricao);
        this.taxaJuros = taxaJuros;
        this.dataVencimento = dataVencimento;
    }

    public double getTaxaJuros() {
        return taxaJuros;
    }

    public void setTaxaJuros(double taxaJuros) {
        this.taxaJuros = taxaJuros;
    }

    public String getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(String dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    @Override
    public String getTipo() {
        return "RENDA_FIXA";
    }

    @Override
    public String toString() {
        return "AtivoFixo{" +
                "ticker='" + ticker + '\'' +
                ", precoAtual=" + precoAtual +
                ", taxaJuros=" + taxaJuros +
                ", dataVencimento='" + dataVencimento + '\'' +
                '}';
    }
}

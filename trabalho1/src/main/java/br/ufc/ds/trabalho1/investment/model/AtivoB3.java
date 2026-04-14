package br.ufc.ds.trabalho1.investment.model;

public class AtivoB3 {
    private final String ticker;
    private final String nome;
    private final String setor;

    public AtivoB3(String ticker, String nome, String setor) {
        this.ticker = ticker;
        this.nome = nome;
        this.setor = setor;
    }

    public String getTicker() {
        return ticker;
    }

    public String getNome() {
        return nome;
    }

    public String getSetor() {
        return setor;
    }
}
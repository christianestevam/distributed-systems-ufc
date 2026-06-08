package br.ufc.ds.trabalho2.model;

import java.io.Serializable;

/**
 * Classe abstrata que representa um ativo genérico (base para "é-um").
 */
public abstract class Ativo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String ticker;
    protected double precoAtual;
    protected String descricao;

    public Ativo(String ticker, double precoAtual, String descricao) {
        this.ticker = ticker;
        this.precoAtual = precoAtual;
        this.descricao = descricao;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public double getPrecoAtual() {
        return precoAtual;
    }

    public void setPrecoAtual(double precoAtual) {
        this.precoAtual = precoAtual;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public abstract String getTipo();

    @Override
    public String toString() {
        return "Ativo{" +
                "ticker='" + ticker + '\'' +
                ", precoAtual=" + precoAtual +
                ", descricao='" + descricao + '\'' +
                ", tipo=" + getTipo() +
                '}';
    }
}

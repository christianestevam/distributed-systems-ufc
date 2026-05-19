package br.ufc.ds.trabalho2.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe 3 de entidades: Ordem de investimento.
 */
public class OrdemInvestimento implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String ordemId;
    private String tipo;  // COMPRA ou VENDA
    private String ticker;
    private long quantidade;
    private double precoUnitario;
    private double precoTotal;
    private String status;  // PENDENTE, EXECUTADA, CANCELADA
    private LocalDateTime dataCriacao;

    public OrdemInvestimento() {
        this.dataCriacao = LocalDateTime.now();
        this.status = "PENDENTE";
    }

    public OrdemInvestimento(String ordemId, String tipo, String ticker, long quantidade, double precoUnitario) {
        this();
        this.ordemId = ordemId;
        this.tipo = tipo;
        this.ticker = ticker;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.precoTotal = quantidade * precoUnitario;
    }

    public String getOrdemId() {
        return ordemId;
    }

    public void setOrdemId(String ordemId) {
        this.ordemId = ordemId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(long quantidade) {
        this.quantidade = quantidade;
        this.precoTotal = quantidade * precoUnitario;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
        this.precoTotal = quantidade * precoUnitario;
    }

    public double getPrecoTotal() {
        return precoTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataCriacao() {
        return dataCriacao.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public String toString() {
        return "OrdemInvestimento{" +
                "ordemId='" + ordemId + '\'' +
                ", tipo='" + tipo + '\'' +
                ", ticker='" + ticker + '\'' +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                ", precoTotal=" + precoTotal +
                ", status='" + status + '\'' +
                '}';
    }
}

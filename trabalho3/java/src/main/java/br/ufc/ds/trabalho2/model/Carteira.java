package br.ufc.ds.trabalho2.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Classe 4 de entidades: Carteira de investimentos.
 * Demonstra agregação "tem-um" com HashMap de ativos.
 */
public class Carteira implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String carteiraId;
    private Map<String, Long> ativos;  // ticker -> quantidade
    private double saldoDisponivel;
    private LocalDateTime dataCriacao;

    public Carteira(String carteiraId, double saldoInicial) {
        this.carteiraId = carteiraId;
        this.ativos = new HashMap<>();
        this.saldoDisponivel = saldoInicial;
        this.dataCriacao = LocalDateTime.now();
    }

    public String getCarteiraId() {
        return carteiraId;
    }

    public void setCarteiraId(String carteiraId) {
        this.carteiraId = carteiraId;
    }

    public Map<String, Long> getAtivos() {
        return ativos;
    }

    public void adicionarAtivo(String ticker, long quantidade) {
        ativos.put(ticker, ativos.getOrDefault(ticker, 0L) + quantidade);
    }

    public void removerAtivo(String ticker, long quantidade) {
        long atual = ativos.getOrDefault(ticker, 0L);
        if (atual >= quantidade) {
            ativos.put(ticker, atual - quantidade);
        }
    }

    public long obterQuantidade(String ticker) {
        return ativos.getOrDefault(ticker, 0L);
    }

    public double getSaldoDisponivel() {
        return saldoDisponivel;
    }

    public void setSaldoDisponivel(double saldoDisponivel) {
        this.saldoDisponivel = saldoDisponivel;
    }

    public double adicionarSaldo(double valor) {
        this.saldoDisponivel += valor;
        return this.saldoDisponivel;
    }

    public double deduzirSaldo(double valor) {
        this.saldoDisponivel -= valor;
        return this.saldoDisponivel;
    }

    public String getDataCriacao() {
        return dataCriacao.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public String toString() {
        return "Carteira{" +
                "carteiraId='" + carteiraId + '\'' +
                ", ativos=" + ativos +
                ", saldoDisponivel=" + saldoDisponivel +
                '}';
    }
}

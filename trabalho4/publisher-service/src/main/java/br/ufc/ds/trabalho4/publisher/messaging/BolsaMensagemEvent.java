package br.ufc.ds.trabalho4.publisher.messaging;

import java.time.Instant;

public class BolsaMensagemEvent {
    private String ticker;
    private double preco;
    private long volume;
    private String origem;
    private Instant receivedAt;

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
}

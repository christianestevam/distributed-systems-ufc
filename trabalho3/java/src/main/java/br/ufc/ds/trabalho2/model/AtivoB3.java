package br.ufc.ds.trabalho2.model;

/**
 * Extensão de Ativo para ativos negociados na B3 (ações).
 * Demonstra composição tipo "é-um".
 */
public class AtivoB3 extends Ativo {
    private static final long serialVersionUID = 1L;
    
    private String segmento;  // ex: "Ibovespa", "Small Caps"
    private long volumeNegociado;

    public AtivoB3(String ticker, double precoAtual, String descricao, String segmento, long volumeNegociado) {
        super(ticker, precoAtual, descricao);
        this.segmento = segmento;
        this.volumeNegociado = volumeNegociado;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public long getVolumeNegociado() {
        return volumeNegociado;
    }

    public void setVolumeNegociado(long volumeNegociado) {
        this.volumeNegociado = volumeNegociado;
    }

    @Override
    public String getTipo() {
        return "ACAO_B3";
    }

    @Override
    public String toString() {
        return "AtivoB3{" +
                "ticker='" + ticker + '\'' +
                ", precoAtual=" + precoAtual +
                ", segmento='" + segmento + '\'' +
                ", volumeNegociado=" + volumeNegociado +
                '}';
    }
}

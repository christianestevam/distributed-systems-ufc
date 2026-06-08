package br.ufc.ds.trabalho3.api.dto;

public record CriarOrdemRequest(
        String ordemId,
        String tipo,
        String ticker,
        long quantidade,
        double precoUnitario
) {
}
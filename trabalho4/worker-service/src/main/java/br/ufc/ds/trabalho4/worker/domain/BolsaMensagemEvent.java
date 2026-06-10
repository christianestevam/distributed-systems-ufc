package br.ufc.ds.trabalho4.worker.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record BolsaMensagemEvent(
        String ticker,
        BigDecimal preco,
        Long volume,
        String bolsa,
        String origem,
        Instant receivedAt,
        String payload
) {
}
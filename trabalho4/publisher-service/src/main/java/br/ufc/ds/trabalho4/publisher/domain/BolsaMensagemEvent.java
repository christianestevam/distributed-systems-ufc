package br.ufc.ds.trabalho4.publisher.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record BolsaMensagemEvent(
        String ticker,
        BigDecimal preco,
        Long volume,
        String bolsa,
        String origem,
        Instant receivedAt
) {
}
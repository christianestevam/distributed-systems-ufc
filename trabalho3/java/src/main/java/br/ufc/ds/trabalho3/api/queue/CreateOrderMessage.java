package br.ufc.ds.trabalho3.api.queue;

public record CreateOrderMessage(
        String investidorId,
        String ordemId,
        String tipo,
        String ticker,
        long quantidade,
        double precoUnitario
) implements QueueMessage {
}

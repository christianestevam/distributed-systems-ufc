package br.ufc.ds.trabalho3.api.queue;

public record AddBalanceMessage(
        String investidorId,
        double valor
) implements QueueMessage {
}

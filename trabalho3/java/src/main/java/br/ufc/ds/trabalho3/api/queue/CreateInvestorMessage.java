package br.ufc.ds.trabalho3.api.queue;

public record CreateInvestorMessage(
        String investidorId,
        String nome,
        String cpf,
        String email,
        String telefone
) implements QueueMessage {
}

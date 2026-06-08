package br.ufc.ds.trabalho3.api.dto;

public record CriarInvestidorRequest(
        String investidorId,
        String nome,
        String cpf,
        String email,
        String telefone
) {
}
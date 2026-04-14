package br.ufc.ds.trabalho1.investment.model;

public class Investidor {
    private final String id;
    private final String nome;
    private final String cpf;

    public Investidor(String id, String nome, String cpf) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }
}

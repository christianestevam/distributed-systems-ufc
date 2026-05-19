package br.ufc.ds.trabalho2.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe 5 de entidades: Investidor (principal).
 * Demonstra agregação "tem-um" com Carteira.
 */
public class Investidor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String investidorId;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private Carteira carteira;  // agregação "tem-um"
    private LocalDateTime dataCadastro;

    public Investidor() {}

    public Investidor(String investidorId, String nome, String cpf, String email, String telefone) {
        this.investidorId = investidorId;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.carteira = new Carteira(investidorId + "_cart", 10000.0);
        this.dataCadastro = LocalDateTime.now();
    }

    public String getInvestidorId() {
        return investidorId;
    }

    public void setInvestidorId(String investidorId) {
        this.investidorId = investidorId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Carteira getCarteira() {
        return carteira;
    }

    public void setCarteira(Carteira carteira) {
        this.carteira = carteira;
    }

    public String getDataCadastro() {
        return dataCadastro.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public String toString() {
        return "Investidor{" +
                "investidorId='" + investidorId + '\'' +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", carteira=" + carteira +
                '}';
    }
}

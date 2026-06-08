package br.ufc.ds.trabalho2.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe 6 de entidades: Conta bancária (agregação "tem-um" com Investidor).
 */
public class Conta implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String contaId;
    private String investidorId;
    private String banco;
    private String agencia;
    private String numeroConta;
    private String tipoConta;  // CORRENTE, POUPANCA, INVESTIMENTO
    private double saldo;
    private LocalDateTime dataAbertura;

    public Conta() {}

    public Conta(String contaId, String investidorId, String banco, String agencia, 
                 String numeroConta, String tipoConta) {
        this.contaId = contaId;
        this.investidorId = investidorId;
        this.banco = banco;
        this.agencia = agencia;
        this.numeroConta = numeroConta;
        this.tipoConta = tipoConta;
        this.saldo = 0.0;
        this.dataAbertura = LocalDateTime.now();
    }

    public String getContaId() {
        return contaId;
    }

    public void setContaId(String contaId) {
        this.contaId = contaId;
    }

    public String getInvestidorId() {
        return investidorId;
    }

    public void setInvestidorId(String investidorId) {
        this.investidorId = investidorId;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    public void setNumeroConta(String numeroConta) {
        this.numeroConta = numeroConta;
    }

    public String getTipoConta() {
        return tipoConta;
    }

    public void setTipoConta(String tipoConta) {
        this.tipoConta = tipoConta;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getDataAbertura() {
        return dataAbertura.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public String toString() {
        return "Conta{" +
                "contaId='" + contaId + '\'' +
                ", investidorId='" + investidorId + '\'' +
                ", banco='" + banco + '\'' +
                ", agencia='" + agencia + '\'' +
                ", numeroConta='" + numeroConta + '\'' +
                ", tipoConta='" + tipoConta + '\'' +
                ", saldo=" + saldo +
                '}';
    }
}

package br.ufc.ds.trabalho2.app;

import br.ufc.ds.trabalho2.model.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class InvestidorServiceImpl extends UnicastRemoteObject implements InvestidorServiceRemote {

    private static final long serialVersionUID = 1L;
    private Map<String, Investidor> investidores;
    private Map<String, OrdemInvestimento> ordens;
    private Map<String, Ativo> ativos;

    public InvestidorServiceImpl() throws RemoteException {
        super();
        this.investidores = new HashMap<>();
        this.ordens = new HashMap<>();
        this.ativos = new HashMap<>();
        ativos.put("PETR4", new AtivoB3("PETR4", 39.10, "Petrobras", "Ibovespa", 1000000));
        ativos.put("VALE3", new AtivoB3("VALE3", 68.30, "Vale", "Ibovespa", 800000));
        ativos.put("ITUB4", new AtivoB3("ITUB4", 31.40, "Itau", "Ibovespa", 500000));
        ativos.put("LTN", new AtivoFixo("LTN", 95.50, "Letra do Tesouro", 5.5, "2025-01-15"));
    }

    @Override
    public Investidor criarInvestidor(String investidorId, String nome, String cpf, String email, String telefone) throws RemoteException {
        Investidor inv = new Investidor(investidorId, nome, cpf, email, telefone);
        investidores.put(investidorId, inv);
        System.out.println("[InvestidorServiceImpl] Investidor criado: " + investidorId);
        return inv;
    }

    @Override
    public Investidor obterInvestidor(String investidorId) throws RemoteException {
        Investidor inv = investidores.get(investidorId);
        if (inv == null) {
            System.out.println("[InvestidorServiceImpl] Investidor nao encontrado: " + investidorId);
            return null;
        }
        System.out.println("[InvestidorServiceImpl] Investidor obtido: " + investidorId);
        return inv;
    }

    @Override
    public OrdemInvestimento criarOrdem(String ordemId, String tipo, String ticker, long quantidade, double precoUnitario) throws RemoteException {
        if (!ativos.containsKey(ticker)) {
            System.out.println("[InvestidorServiceImpl] Ativo nao encontrado: " + ticker);
            return null;
        }
        OrdemInvestimento ordem = new OrdemInvestimento(ordemId, tipo, ticker, quantidade, precoUnitario);
        ordem.setStatus("EXECUTADA");
        ordens.put(ordemId, ordem);
        System.out.println("[InvestidorServiceImpl] Ordem criada: " + ordemId);
        return ordem;
    }

    @Override
    public OrdemInvestimento[] obterOrdensDoInvestidor(String investidorId) throws RemoteException {
        OrdemInvestimento[] resultado = ordens.values().stream()
            .filter(o -> o.getOrdemId().startsWith(investidorId))
            .toArray(OrdemInvestimento[]::new);
        System.out.println("[InvestidorServiceImpl] Ordens obtidas: " + resultado.length);
        return resultado;
    }

    @Override
    public double adicionarSaldoCarteira(String investidorId, double valor) throws RemoteException {
        Investidor inv = investidores.get(investidorId);
        if (inv == null) {
            System.out.println("[InvestidorServiceImpl] Investidor nao encontrado: " + investidorId);
            return 0;
        }
        double novoSaldo = inv.getCarteira().adicionarSaldo(valor);
        System.out.println("[InvestidorServiceImpl] Saldo adicionado: " + novoSaldo);
        return novoSaldo;
    }

    @Override
    public Ativo obterAtivo(String ticker) throws RemoteException {
        Ativo ativo = ativos.get(ticker);
        if (ativo == null) {
            System.out.println("[InvestidorServiceImpl] Ativo nao encontrado: " + ticker);
            return null;
        }
        System.out.println("[InvestidorServiceImpl] Ativo obtido: " + ticker);
        return ativo;
    }
}

package main.java.br.ufc.ds.trabalho2.app;

import br.ufc.ds.trabalho2.model.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço de negócio com métodos remotos para operações com investimentos.
 * Mínimo 4 métodos remotos (temos 6).
 */
public class InvestidorService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Investidor> investidores;
    private Map<String, OrdemInvestimento> ordens;
    private Map<String, Ativo> ativos;

    public InvestidorService() {
        this.investidores = new HashMap<>();
        this.ordens = new HashMap<>();
        this.ativos = new HashMap<>();
        
        // Inicializa ativos
        ativos.put("PETR4", new AtivoB3("PETR4", 39.10, "Petrobras", "Ibovespa", 1000000));
        ativos.put("VALE3", new AtivoB3("VALE3", 68.30, "Vale", "Ibovespa", 800000));
        ativos.put("ITUB4", new AtivoB3("ITUB4", 31.40, "Itau", "Ibovespa", 500000));
        ativos.put("LTN", new AtivoFixo("LTN", 95.50, "Letra do Tesouro", 5.5, "2025-01-15"));
    }

    /**
     * Método remoto 1: Criar novo investidor.
     * Retorna passagem por valor (objeto serializado).
     */
    public Investidor criarInvestidor(String investidorId, String nome, String cpf, String email, String telefone) {
        Investidor inv = new Investidor(investidorId, nome, cpf, email, telefone);
        investidores.put(investidorId, inv);
        System.out.println("[InvestidorService] Investidor criado: " + investidorId);
        return inv;
    }

    /**
     * Método remoto 2: Obter investidor pelo ID.
     * Passagem por valor (cópia serializada).
     */
    public Investidor obterInvestidor(String investidorId) {
        Investidor inv = investidores.get(investidorId);
        if (inv == null) {
            System.out.println("[InvestidorService] Investidor nao encontrado: " + investidorId);
            return null;
        }
        System.out.println("[InvestidorService] Investidor obtido: " + investidorId);
        return inv;
    }

    /**
     * Método remoto 3: Criar ordem de investimento.
     * Passagem por valor com argumentos primitivos e estruturados.
     */
    public OrdemInvestimento criarOrdem(String ordemId, String tipo, String ticker, long quantidade, double precoUnitario) {
        if (!ativos.containsKey(ticker)) {
            System.out.println("[InvestidorService] Ativo nao encontrado: " + ticker);
            return null;
        }
        
        OrdemInvestimento ordem = new OrdemInvestimento(ordemId, tipo, ticker, quantidade, precoUnitario);
        ordem.setStatus("EXECUTADA");
        ordens.put(ordemId, ordem);
        System.out.println("[InvestidorService] Ordem criada: " + ordemId);
        return ordem;
    }

    /**
     * Método remoto 4: Obter histórico de ordens.
     * Retorna array de ordens.
     */
    public OrdemInvestimento[] obterOrdensDoInvestidor(String investidorId) {
        OrdemInvestimento[] resultado = ordens.values().stream()
            .filter(o -> o.getOrdemId().startsWith(investidorId))
            .toArray(OrdemInvestimento[]::new);
        System.out.println("[InvestidorService] Ordens obtidas: " + resultado.length);
        return resultado;
    }

    /**
     * Método remoto 5: Adicionar saldo à carteira.
     * Passagem por valor com tipos primitivos.
     */
    public double adicionarSaldoCarteira(String investidorId, double valor) {
        Investidor inv = investidores.get(investidorId);
        if (inv == null) {
            System.out.println("[InvestidorService] Investidor nao encontrado: " + investidorId);
            return 0;
        }
        double novoSaldo = inv.getCarteira().adicionarSaldo(valor);
        System.out.println("[InvestidorService] Saldo adicionado: " + novoSaldo);
        return novoSaldo;
    }

    /**
     * Método remoto 6: Obter informações de um ativo.
     * Retorna polimorficamente AtivoB3 ou AtivoFixo.
     */
    public Ativo obterAtivo(String ticker) {
        Ativo ativo = ativos.get(ticker);
        if (ativo == null) {
            System.out.println("[InvestidorService] Ativo nao encontrado: " + ticker);
            return null;
        }
        System.out.println("[InvestidorService] Ativo obtido: " + ticker);
        return ativo;
    }
}

package br.ufc.ds.trabalho2.app;

import br.ufc.ds.trabalho2.model.*;

/**
 * Interface para serviço remoto de investimentos.
 * Implementa 6 métodos remotos (requisito mínimo: 4).
 */
public interface InvestidorServiceRemote {
    /**
     * Método remoto 1: criar novo investidor.
     * Demonstra passagem por VALOR (argumentos primitivos e String).
     */
    Investidor criarInvestidor(String investidorId, String nome, String cpf, String email, String telefone);
    
    /**
     * Método remoto 2: obter investidor existente.
     * Demonstra passagem por REFERÊNCIA (RemoteObjectRef) na requisição e
     * passagem por VALOR (Investidor serializado) na resposta.
     */
    Investidor obterInvestidor(String investidorId);
    
    /**
     * Método remoto 3: criar ordem de investimento.
     */
    OrdemInvestimento criarOrdem(String ordemId, String tipo, String ticker, long quantidade, double precoUnitario);
    
    /**
     * Método remoto 4: obter ordens do investidor.
     */
    OrdemInvestimento[] obterOrdensDoInvestidor(String investidorId);
    
    /**
     * Método remoto 5: adicionar saldo à carteira.
     */
    double adicionarSaldoCarteira(String investidorId, double valor);
    
    /**
     * Método remoto 6: obter ativo por ticker.
     */
    Ativo obterAtivo(String ticker);
}

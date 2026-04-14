package br.ufc.ds.trabalho1.investment.service;

import br.ufc.ds.trabalho1.investment.model.OrdemInvestimento;

public class OrdemService {
    public OrdemInvestimento criarOrdem(String ordemId, String ticker, String tipo, long quantidade, double precoUnitario) {
        if (ordemId == null || ordemId.isBlank()) {
            throw new IllegalArgumentException("ordemId obrigatorio");
        }
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("ticker obrigatorio");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("quantidade deve ser positiva");
        }
        if (precoUnitario <= 0) {
            throw new IllegalArgumentException("precoUnitario deve ser positivo");
        }
        if (!"COMPRA".equalsIgnoreCase(tipo) && !"VENDA".equalsIgnoreCase(tipo)) {
            throw new IllegalArgumentException("tipo deve ser COMPRA ou VENDA");
        }
        return new OrdemInvestimento(ordemId, ticker.toUpperCase(), tipo.toUpperCase(), quantidade, precoUnitario);
    }
}

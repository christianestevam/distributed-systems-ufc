package br.ufc.ds.trabalho1.investment.service;

import br.ufc.ds.trabalho1.investment.model.OrdemInvestimento;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CarteiraService {
    private final Map<String, Long> posicoes = new HashMap<>();

    public synchronized void aplicarOrdem(OrdemInvestimento ordem) {
        long atual = posicoes.getOrDefault(ordem.getTicker(), 0L);
        if ("COMPRA".equalsIgnoreCase(ordem.getTipo())) {
            posicoes.put(ordem.getTicker(), atual + ordem.getQuantidade());
            return;
        }
        if ("VENDA".equalsIgnoreCase(ordem.getTipo())) {
            posicoes.put(ordem.getTicker(), Math.max(0, atual - ordem.getQuantidade()));
            return;
        }
        throw new IllegalArgumentException("Tipo de ordem invalido: " + ordem.getTipo());
    }

    public synchronized Map<String, Long> getPosicoes() {
        return Collections.unmodifiableMap(new HashMap<>(posicoes));
    }
}

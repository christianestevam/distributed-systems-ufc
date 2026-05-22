package br.ufc.ds.trabalho2.rmi;

import br.ufc.ds.trabalho2.model.*;
import java.io.IOException;

/**
 * Classe para serializar/desserializar objetos de domínio para Protocol Buffers.
 */
public class ProtobufSerializer {

    // ========== SERIALIZAÇÃO (Objeto → Protobuf) ==========

    public static br.ufc.ds.trabalho2.rmi.pb.Investidor serializeInvestidor(br.ufc.ds.trabalho2.model.Investidor inv) {
        if (inv == null) return null;
        
        return br.ufc.ds.trabalho2.rmi.pb.Investidor.newBuilder()
                .setInvestidorId(inv.getInvestidorId())
                .setNome(inv.getNome())
                .setCpf(inv.getCpf())
                .setEmail(inv.getEmail())
                .setTelefone(inv.getTelefone())
                .setCarteira(serializeCarteira(inv.getCarteira()))
                .setDataCadastro(inv.getDataCadastro())
                .build();
    }

    public static br.ufc.ds.trabalho2.rmi.pb.Carteira serializeCarteira(br.ufc.ds.trabalho2.model.Carteira carteira) {
        if (carteira == null) return null;
        
        br.ufc.ds.trabalho2.rmi.pb.Carteira.Builder builder = br.ufc.ds.trabalho2.rmi.pb.Carteira.newBuilder()
                .setCarteiraId(carteira.getCarteiraId())
                .setSaldoDisponivel(carteira.getSaldoDisponivel())
                .setDataCriacao(carteira.getDataCriacao());
        
        for (String ticker : carteira.getAtivos().keySet()) {
            builder.putAtivos(ticker, carteira.getAtivos().get(ticker));
        }
        
        return builder.build();
    }

    public static br.ufc.ds.trabalho2.rmi.pb.OrdemInvestimento serializeOrdem(br.ufc.ds.trabalho2.model.OrdemInvestimento ordem) {
        if (ordem == null) return null;
        
        return br.ufc.ds.trabalho2.rmi.pb.OrdemInvestimento.newBuilder()
                .setOrdemId(ordem.getOrdemId())
                .setTipo(ordem.getTipo())
                .setTicker(ordem.getTicker())
                .setQuantidade(ordem.getQuantidade())
                .setPrecoUnitario(ordem.getPrecoUnitario())
                .setPrecoTotal(ordem.getPrecoTotal())
                .setStatus(ordem.getStatus())
                .setDataCriacao(ordem.getDataCriacao())
                .build();
    }

    public static br.ufc.ds.trabalho2.rmi.pb.Ativo serializeAtivo(br.ufc.ds.trabalho2.model.Ativo ativo) {
        if (ativo == null) return null;
        
        br.ufc.ds.trabalho2.rmi.pb.Ativo.Builder builder = br.ufc.ds.trabalho2.rmi.pb.Ativo.newBuilder()
                .setTicker(ativo.getTicker())
                .setPrecoAtual(ativo.getPrecoAtual())
                .setDescricao(ativo.getDescricao())
                .setTipo(ativo.getTipo());
        
        if (ativo instanceof br.ufc.ds.trabalho2.model.AtivoB3) {
            br.ufc.ds.trabalho2.model.AtivoB3 b3 = (br.ufc.ds.trabalho2.model.AtivoB3) ativo;
            builder.setAtivoB3(br.ufc.ds.trabalho2.rmi.pb.AtivoB3.newBuilder()
                    .setTicker(b3.getTicker())
                    .setPrecoAtual(b3.getPrecoAtual())
                    .setDescricao(b3.getDescricao())
                    .setSegmento(b3.getSegmento())
                    .setVolumeNegociado(b3.getVolumeNegociado())
                    .build());
        } else if (ativo instanceof br.ufc.ds.trabalho2.model.AtivoFixo) {
            br.ufc.ds.trabalho2.model.AtivoFixo fixo = (br.ufc.ds.trabalho2.model.AtivoFixo) ativo;
            builder.setAtivoFixo(br.ufc.ds.trabalho2.rmi.pb.AtivoFixo.newBuilder()
                    .setTicker(fixo.getTicker())
                    .setPrecoAtual(fixo.getPrecoAtual())
                    .setDescricao(fixo.getDescricao())
                    .setTaxaJuros(fixo.getTaxaJuros())
                    .setDataVencimento(fixo.getDataVencimento())
                    .build());
        }
        
        return builder.build();
    }

    // ========== DESSERIALIZAÇÃO (Protobuf → Objeto) ==========

    public static br.ufc.ds.trabalho2.model.Investidor deserializeInvestidor(br.ufc.ds.trabalho2.rmi.pb.Investidor pb) {
        if (pb == null || pb.getInvestidorId().isEmpty()) return null;
        
        br.ufc.ds.trabalho2.model.Investidor inv = new br.ufc.ds.trabalho2.model.Investidor(
                pb.getInvestidorId(),
                pb.getNome(),
                pb.getCpf(),
                pb.getEmail(),
                pb.getTelefone()
        );
        
        if (pb.hasCarteira()) {
            inv.setCarteira(deserializeCarteira(pb.getCarteira()));
        }
        
        return inv;
    }

    public static br.ufc.ds.trabalho2.model.Carteira deserializeCarteira(br.ufc.ds.trabalho2.rmi.pb.Carteira pb) {
        if (pb == null) return null;
        
        br.ufc.ds.trabalho2.model.Carteira carteira = new br.ufc.ds.trabalho2.model.Carteira(pb.getCarteiraId(), pb.getSaldoDisponivel());
        for (String ticker : pb.getAtivosMap().keySet()) {
            carteira.adicionarAtivo(ticker, pb.getAtivosMap().get(ticker));
        }
        return carteira;
    }

    public static br.ufc.ds.trabalho2.model.OrdemInvestimento deserializeOrdem(br.ufc.ds.trabalho2.rmi.pb.OrdemInvestimento pb) {
        if (pb == null || pb.getOrdemId().isEmpty()) return null;
        
        br.ufc.ds.trabalho2.model.OrdemInvestimento ordem = new br.ufc.ds.trabalho2.model.OrdemInvestimento(
                pb.getOrdemId(),
                pb.getTipo(),
                pb.getTicker(),
                pb.getQuantidade(),
                pb.getPrecoUnitario()
        );
        ordem.setStatus(pb.getStatus());
        return ordem;
    }

    public static br.ufc.ds.trabalho2.model.Ativo deserializeAtivo(br.ufc.ds.trabalho2.rmi.pb.Ativo pb) {
        if (pb == null || pb.getTicker().isEmpty()) return null;
        
        if (pb.getAtivoB3() != null && !pb.getAtivoB3().getTicker().isEmpty()) {
            br.ufc.ds.trabalho2.rmi.pb.AtivoB3 b3 = pb.getAtivoB3();
            return new br.ufc.ds.trabalho2.model.AtivoB3(
                    b3.getTicker(),
                    b3.getPrecoAtual(),
                    b3.getDescricao(),
                    b3.getSegmento(),
                    b3.getVolumeNegociado()
            );
        } else if (pb.getAtivoFixo() != null && !pb.getAtivoFixo().getTicker().isEmpty()) {
            br.ufc.ds.trabalho2.rmi.pb.AtivoFixo fixo = pb.getAtivoFixo();
            return new br.ufc.ds.trabalho2.model.AtivoFixo(
                    fixo.getTicker(),
                    fixo.getPrecoAtual(),
                    fixo.getDescricao(),
                    fixo.getTaxaJuros(),
                    fixo.getDataVencimento()
            );
        }
        
        return null;
    }
}

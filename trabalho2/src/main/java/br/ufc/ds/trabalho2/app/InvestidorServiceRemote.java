package br.ufc.ds.trabalho2.app;

import br.ufc.ds.trabalho2.model.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InvestidorServiceRemote extends Remote {
    Investidor criarInvestidor(String investidorId, String nome, String cpf, String email, String telefone) throws RemoteException;
    Investidor obterInvestidor(String investidorId) throws RemoteException;
    OrdemInvestimento criarOrdem(String ordemId, String tipo, String ticker, long quantidade, double precoUnitario) throws RemoteException;
    OrdemInvestimento[] obterOrdensDoInvestidor(String investidorId) throws RemoteException;
    double adicionarSaldoCarteira(String investidorId, double valor) throws RemoteException;
    Ativo obterAtivo(String ticker) throws RemoteException;
}

package br.ufc.ds.trabalho2.rmi;

import br.ufc.ds.trabalho2.app.InvestidorServiceRemote;
import br.ufc.ds.trabalho2.model.*;
import java.rmi.Naming;
import java.util.Scanner;

/**
 * Cliente que usa Java RMI Registry to lookup the remote service.
 */
public class RMIClient {
    private InvestidorServiceRemote service;

    public RMIClient(String host, int port) throws Exception {
        String url = String.format("rmi://%s:%d/investidor_service", host, port);
        this.service = (InvestidorServiceRemote) Naming.lookup(url);
    }

    public InvestidorServiceRemote getService() {
        return service;
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 1099;

        RMIClient client = new RMIClient(host, port);
        InvestidorServiceRemote svc = client.getService();
        System.out.println("[RMIClient] Obtained remote service proxy");
        System.out.println("Digite o numero da opcao e forneca os dados conforme solicitado.");
        System.out.println("Para criar ordens use um ticker valido, ex: PETR4, VALE3, ITUB4, LTN.");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Menu RMI Cliente ===");
            System.out.println("1) criarInvestidor - criar novo investidor com dados pessoais");
            System.out.println("2) obterInvestidor - recuperar investidor existente pelo ID");
            System.out.println("3) criarOrdem - criar ordem de investimento para um ticker existente");
            System.out.println("4) obterOrdensDoInvestidor - listar ordens associadas ao investidor");
            System.out.println("5) adicionarSaldoCarteira - adicionar saldo na carteira de um investidor");
            System.out.println("6) obterAtivo - consultar detalhes de um ativo pelo ticker");
            System.out.println("7) sair");
            System.out.print("Escolha uma opcao (1-7): ");

                String opt = scanner.nextLine().trim();
                try {
                    switch (opt) {
                        case "1": {
                            System.out.println("Criar um investidor exige um ID unico e dados pessoais.");
                            System.out.print("investidorId (ex: inv1): ");
                            String id = scanner.nextLine().trim();
                            System.out.print("nome completo: ");
                            String nome = scanner.nextLine().trim();
                            System.out.print("cpf (ex: 000.000.000-00): ");
                            String cpf = scanner.nextLine().trim();
                            System.out.print("email: ");
                            String email = scanner.nextLine().trim();
                            System.out.print("telefone (ex: 99999-0000): ");
                            String tel = scanner.nextLine().trim();
                            Investidor inv = svc.criarInvestidor(id, nome, cpf, email, tel);
                            System.out.println("Criado: " + inv);
                            break;
                        }
                        case "2": {
                            System.out.println("Recuperar um investidor existente pelo ID usado ao criar.");
                            System.out.print("investidorId (ex: inv1): ");
                            String id = scanner.nextLine().trim();
                            Investidor inv = svc.obterInvestidor(id);
                            System.out.println("Resultado: " + inv);
                            break;
                        }
                        case "3": {
                            System.out.println("Use tipo COMPRA ou VENDA. Tickers validos: PETR4, VALE3, ITUB4, LTN.");
                            System.out.print("ordemId (ex: ORD123): ");
                            String ordemId = scanner.nextLine().trim();
                            System.out.print("tipo (COMPRA/VENDA): ");
                            String tipo = scanner.nextLine().trim();
                            System.out.print("ticker (PETR4, VALE3, ITUB4, LTN): ");
                            String ticker = scanner.nextLine().trim();
                            System.out.print("quantidade (numero inteiro): ");
                            long q = Long.parseLong(scanner.nextLine().trim());
                            System.out.print("precoUnitario (numero decimal): ");
                            double p = Double.parseDouble(scanner.nextLine().trim());
                            OrdemInvestimento ordem = svc.criarOrdem(ordemId, tipo, ticker, q, p);
                            System.out.println("Ordem criada: " + ordem);
                            break;
                        }
                        case "4": {
                            System.out.println("Listar todas as ordens de um investidor especifico.");
                            System.out.print("investidorId (ex: inv1): ");
                            String id = scanner.nextLine().trim();
                            OrdemInvestimento[] ords = svc.obterOrdensDoInvestidor(id);
                            System.out.println("Ordens encontradas: " + (ords == null ? 0 : ords.length));
                            if (ords != null) {
                                for (OrdemInvestimento o : ords) System.out.println(o);
                            }
                            break;
                        }
                        case "5": {
                            System.out.println("Adicionar saldo a carteira de um investidor existente.");
                            System.out.print("investidorId (ex: inv1): ");
                            String id = scanner.nextLine().trim();
                            System.out.print("valor a adicionar (ex: 1000.50): ");
                            double val = Double.parseDouble(scanner.nextLine().trim());
                            double novo = svc.adicionarSaldoCarteira(id, val);
                            System.out.println("Novo saldo: " + novo);
                            break;
                        }
                        case "6": {
                            System.out.println("TICKERS validos atualmente: PETR4, VALE3, ITUB4, LTN");
                            System.out.print("ticker para consulta: ");
                            String t = scanner.nextLine().trim();
                            Ativo a = svc.obterAtivo(t);
                            System.out.println("Ativo: " + a);
                            break;
                        }
                        case "7": {
                            System.out.println("Saindo...");
                            scanner.close();
                            return;
                        }
                        default:
                            System.out.println("Opcao invalida");
                    }
                } catch (Exception e) {
                    System.err.println("Erro na chamada remota: " + e.getMessage());
                }
            }
    }
}

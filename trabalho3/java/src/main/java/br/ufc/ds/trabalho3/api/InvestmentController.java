package br.ufc.ds.trabalho3.api;

import br.ufc.ds.trabalho2.app.InvestidorServiceImpl;
import br.ufc.ds.trabalho2.model.Ativo;
import br.ufc.ds.trabalho2.model.Investidor;
import br.ufc.ds.trabalho2.model.OrdemInvestimento;
import br.ufc.ds.trabalho3.api.dto.AdicionarSaldoRequest;
import br.ufc.ds.trabalho3.api.dto.CriarInvestidorRequest;
import br.ufc.ds.trabalho3.api.dto.CriarOrdemRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1")
public class InvestmentController {

    private final InvestidorServiceImpl service;

    public InvestmentController(InvestidorServiceImpl service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("service", "trabalho3-api");
        return response;
    }

    @PostMapping("/investidores")
    public ResponseEntity<Investidor> criarInvestidor(@RequestBody CriarInvestidorRequest request) {
        Investidor investidor = service.criarInvestidor(
                request.investidorId(),
                request.nome(),
                request.cpf(),
                request.email(),
                request.telefone());
        return ResponseEntity.status(HttpStatus.CREATED).body(investidor);
    }

    @GetMapping("/investidores/{investidorId}")
    public ResponseEntity<Investidor> obterInvestidor(@PathVariable String investidorId) {
        Investidor investidor = service.obterInvestidor(investidorId);
        if (investidor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(investidor);
    }

    @PostMapping("/investidores/{investidorId}/saldo")
    public ResponseEntity<Map<String, Object>> adicionarSaldoCarteira(
            @PathVariable String investidorId,
            @RequestBody AdicionarSaldoRequest request) {
        Investidor investidor = service.obterInvestidor(investidorId);
        if (investidor == null) {
            return ResponseEntity.notFound().build();
        }

        double novoSaldo = service.adicionarSaldoCarteira(investidorId, request.valor());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("investidorId", investidorId);
        response.put("novoSaldo", novoSaldo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/investidores/{investidorId}/ordens")
    public ResponseEntity<OrdemInvestimento> criarOrdem(
            @PathVariable String investidorId,
            @RequestBody CriarOrdemRequest request) {
        Investidor investidor = service.obterInvestidor(investidorId);
        if (investidor == null) {
            return ResponseEntity.notFound().build();
        }

        String ordemId = request.ordemId().startsWith(investidorId)
                ? request.ordemId()
                : investidorId + "_" + request.ordemId();

        OrdemInvestimento ordem = service.criarOrdem(
                ordemId,
                request.tipo(),
                request.ticker(),
                request.quantidade(),
                request.precoUnitario());

        if (ordem == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ordem);
    }

    @GetMapping("/investidores/{investidorId}/ordens")
    public ResponseEntity<List<OrdemInvestimento>> obterOrdensDoInvestidor(@PathVariable String investidorId) {
        Investidor investidor = service.obterInvestidor(investidorId);
        if (investidor == null) {
            return ResponseEntity.notFound().build();
        }

        OrdemInvestimento[] ordens = service.obterOrdensDoInvestidor(investidorId);
        return ResponseEntity.ok(Arrays.asList(ordens));
    }

    @GetMapping("/ativos")
    public ResponseEntity<Map<String, Ativo>> listarAtivos() {
        return ResponseEntity.ok(service.obterTodosAtivos());
    }

    @GetMapping("/ativos/{ticker}")
    public ResponseEntity<Ativo> obterAtivo(@PathVariable String ticker) {
        Ativo ativo = service.obterAtivo(ticker);
        if (ativo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ativo);
    }
}
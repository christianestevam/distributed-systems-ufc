package br.ufc.ds.trabalho3.api;

import br.ufc.ds.trabalho2.app.InvestidorServiceImpl;
import br.ufc.ds.trabalho2.model.Ativo;
import br.ufc.ds.trabalho2.model.Investidor;
import br.ufc.ds.trabalho2.model.OrdemInvestimento;
import br.ufc.ds.trabalho3.api.dto.AdicionarSaldoRequest;
import br.ufc.ds.trabalho3.api.dto.CriarInvestidorRequest;
import br.ufc.ds.trabalho3.api.dto.CriarOrdemRequest;
import br.ufc.ds.trabalho3.api.queue.AddBalanceMessage;
import br.ufc.ds.trabalho3.api.queue.CreateInvestorMessage;
import br.ufc.ds.trabalho3.api.queue.CreateOrderMessage;
import br.ufc.ds.trabalho3.api.queue.MessageQueue;
import br.ufc.ds.trabalho3.api.queue.MessageQueueProducer;
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
    private final MessageQueueProducer queueProducer;
    private final MessageQueue messageQueue;

    public InvestmentController(InvestidorServiceImpl service,
                                MessageQueueProducer queueProducer,
                                MessageQueue messageQueue) {
        this.service = service;
        this.queueProducer = queueProducer;
        this.messageQueue = messageQueue;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("service", "trabalho3-api");
        return response;
    }

    @PostMapping("/investidores")
    public ResponseEntity<Map<String, Object>> criarInvestidor(@RequestBody CriarInvestidorRequest request) {
        queueProducer.produce(new CreateInvestorMessage(
                request.investidorId(),
                request.nome(),
                request.cpf(),
                request.email(),
                request.telefone()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "accepted");
        response.put("message", "Investidor criado de forma assíncrona");
        response.put("investidorId", request.investidorId());
        response.put("queuePending", messageQueue.size());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/investidores/{investidorId}")
    public ResponseEntity<Investidor> obterInvestidor(@PathVariable("investidorId") String investidorId) {
        Investidor investidor = service.obterInvestidor(investidorId);
        if (investidor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(investidor);
    }

    @PostMapping("/investidores/{investidorId}/saldo")
    public ResponseEntity<Map<String, Object>> adicionarSaldoCarteira(
            @PathVariable("investidorId") String investidorId,
            @RequestBody AdicionarSaldoRequest request) {
        Investidor investidor = service.obterInvestidor(investidorId);
        if (investidor == null) {
            return ResponseEntity.notFound().build();
        }

        queueProducer.produce(new AddBalanceMessage(investidorId, request.valor()));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "accepted");
        response.put("message", "Solicitação de adição de saldo enfileirada");
        response.put("investidorId", investidorId);
        response.put("queuePending", messageQueue.size());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/investidores/{investidorId}/ordens")
    public ResponseEntity<Map<String, Object>> criarOrdem(
            @PathVariable("investidorId") String investidorId,
            @RequestBody CriarOrdemRequest request) {
        Investidor investidor = service.obterInvestidor(investidorId);
        if (investidor == null) {
            return ResponseEntity.notFound().build();
        }

        queueProducer.produce(new CreateOrderMessage(
                investidorId,
                request.ordemId(),
                request.tipo(),
                request.ticker(),
                request.quantidade(),
                request.precoUnitario()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "accepted");
        response.put("message", "Ordem criada de forma assíncrona");
        response.put("investidorId", investidorId);
        response.put("orderId", request.ordemId());
        response.put("queuePending", messageQueue.size());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/investidores/{investidorId}/ordens")
    public ResponseEntity<List<OrdemInvestimento>> obterOrdensDoInvestidor(@PathVariable("investidorId") String investidorId) {
        Investidor investidor = service.obterInvestidor(investidorId);
        if (investidor == null) {
            return ResponseEntity.notFound().build();
        }

        OrdemInvestimento[] ordens = service.obterOrdensDoInvestidor(investidorId);
        return ResponseEntity.ok(Arrays.asList(ordens));
    }

    @GetMapping("/queue/status")
    public ResponseEntity<Map<String, Object>> queueStatus() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("pendingCount", messageQueue.size());
        response.put("pendingMessages", messageQueue.peekAll());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ativos")
    public ResponseEntity<Map<String, Ativo>> listarAtivos() {
        return ResponseEntity.ok(service.obterTodosAtivos());
    }

    @GetMapping("/ativos/{ticker}")
    public ResponseEntity<Ativo> obterAtivo(@PathVariable("ticker") String ticker) {
        Ativo ativo = service.obterAtivo(ticker);
        if (ativo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ativo);
    }
}
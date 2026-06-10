package br.ufc.ds.trabalho4.api.web;

import br.ufc.ds.trabalho4.api.domain.MensagemProcessada;
import br.ufc.ds.trabalho4.api.repository.MensagemProcessadaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MensagemController {

    private final MensagemProcessadaRepository repository;

    public MensagemController(MensagemProcessadaRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("service", "api-service");
        response.put("timestamp", Instant.now());
        return response;
    }

    @GetMapping("/mensagens")
    public Page<MensagemProcessada> listar(
            @RequestParam(required = false) String ticker,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (ticker != null && !ticker.isBlank()) {
            return repository.findByTickerContainingIgnoreCaseOrderByProcessedAtDesc(ticker, pageable);
        }
        return repository.findAllByOrderByProcessedAtDesc(pageable);
    }

    @GetMapping("/mensagens/{id}")
    public MensagemProcessada obter(@PathVariable("id") Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensagem nao encontrada: " + id));
    }

    @GetMapping("/mensagens/total")
    public Map<String, Object> total() {
        return Map.of("total", repository.count());
    }
}
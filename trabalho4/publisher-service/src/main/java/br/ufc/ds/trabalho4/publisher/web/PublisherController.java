package br.ufc.ds.trabalho4.publisher.web;

import br.ufc.ds.trabalho4.publisher.service.BolsaPublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/publisher")
public class PublisherController {

    private final BolsaPublisherService service;

    public PublisherController(BolsaPublisherService service) {
        this.service = service;
    }

    @PostMapping("/teste")
    public ResponseEntity<Map<String, Object>> publicarLoteTeste(@RequestParam(defaultValue = "10") int quantidade) throws Exception {
        List<br.ufc.ds.trabalho4.publisher.domain.BolsaMensagemEvent> mensagens = service.carregarMensagensDeTeste();
        List<br.ufc.ds.trabalho4.publisher.domain.BolsaMensagemEvent> lote = mensagens.stream().limit(quantidade).toList();
        int publicadas = service.publicarMensagens(lote);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("publicadas", publicadas);
        response.put("queue", "bolsa.mensagens");
        return ResponseEntity.ok(response);
    }
}
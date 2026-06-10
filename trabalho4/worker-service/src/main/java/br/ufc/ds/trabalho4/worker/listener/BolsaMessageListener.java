package br.ufc.ds.trabalho4.worker.listener;

import br.ufc.ds.trabalho4.worker.config.RabbitConfig;
import br.ufc.ds.trabalho4.worker.domain.BolsaMensagemEvent;
import br.ufc.ds.trabalho4.worker.domain.MensagemProcessada;
import br.ufc.ds.trabalho4.worker.repository.MensagemProcessadaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class BolsaMessageListener {

    private final ObjectMapper objectMapper;
    private final MensagemProcessadaRepository repository;

    public BolsaMessageListener(ObjectMapper objectMapper, MensagemProcessadaRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void consumir(String payload) throws Exception {
        try {
            BolsaMensagemEvent event = objectMapper.readValue(payload, BolsaMensagemEvent.class);

            MensagemProcessada entity = new MensagemProcessada();
            entity.setTicker(event.ticker());
            entity.setPreco(event.preco() != null ? event.preco() : BigDecimal.ZERO);
            entity.setVolume(event.volume());
            entity.setBolsa(event.bolsa());
            entity.setOrigem(event.origem());
            entity.setStatus("PROCESSADA");
            entity.setPayload(payload);
            entity.setReceivedAt(event.receivedAt() != null ? event.receivedAt() : Instant.now());
            entity.setProcessedAt(Instant.now());

            repository.save(entity);
        } catch (Exception e) {
            // don't requeue — send to DLQ
            throw new AmqpRejectAndDontRequeueException("Failed to process message", e);
        }
    }
}
package br.ufc.ds.trabalho4.publisher.messaging;

import br.ufc.ds.trabalho4.publisher.config.RabbitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BolsaMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public BolsaMessagePublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(BolsaMensagemEvent event) throws Exception {
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, objectMapper.writeValueAsString(event));
    }

    public void publishBatch(List<BolsaMensagemEvent> events) throws Exception {
        for (BolsaMensagemEvent event : events) {
            publish(event);
        }
    }
}

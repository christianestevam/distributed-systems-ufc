package br.ufc.ds.trabalho4.publisher.service;

import br.ufc.ds.trabalho4.publisher.config.RabbitConfig;
import br.ufc.ds.trabalho4.publisher.domain.BolsaMensagemEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class BolsaPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public BolsaPublisherService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public List<BolsaMensagemEvent> carregarMensagensDeTeste() throws Exception {
        try (InputStream inputStream = new ClassPathResource("test-data/mensagens-bolsa.json").getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<BolsaMensagemEvent>>() {});
        }
    }

    public int publicarMensagens(List<BolsaMensagemEvent> mensagens) throws Exception {
        for (BolsaMensagemEvent mensagem : mensagens) {
            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, objectMapper.writeValueAsString(mensagem));
        }
        return mensagens.size();
    }
}
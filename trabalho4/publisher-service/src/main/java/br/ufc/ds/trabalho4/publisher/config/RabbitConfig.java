package br.ufc.ds.trabalho4.publisher.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "bolsa.mensagens";
    public static final String DLQ_NAME = QUEUE_NAME + ".dlq";

    @Bean
    public Queue bolsaQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "");
        args.put("x-dead-letter-routing-key", DLQ_NAME);
        return new Queue(QUEUE_NAME, true, false, false, args);
    }

    @Bean
    public Queue bolsaDlq() {
        return new Queue(DLQ_NAME, true);
    }
}
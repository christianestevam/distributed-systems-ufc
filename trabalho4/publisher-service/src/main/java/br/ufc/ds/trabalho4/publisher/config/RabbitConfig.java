package br.ufc.ds.trabalho4.publisher.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "bolsa.mensagens";

    @Bean
    public Queue bolsaQueue() {
        return new Queue(QUEUE_NAME, true);
    }
}
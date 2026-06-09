package br.ufc.ds.trabalho3.api.queue;

import org.springframework.stereotype.Component;

@Component
public class MessageQueueProducer {

    private final MessageQueue messageQueue;

    public MessageQueueProducer(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public void produce(QueueMessage message) {
        messageQueue.enqueue(message);
    }
}

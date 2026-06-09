package br.ufc.ds.trabalho3.api.queue;

import java.util.List;

public interface MessageQueue {
    void enqueue(QueueMessage message);
    QueueMessage poll();
    int size();
    List<QueueMessage> peekAll();
}

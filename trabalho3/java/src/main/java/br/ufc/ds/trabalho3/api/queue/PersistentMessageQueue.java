package br.ufc.ds.trabalho3.api.queue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class PersistentMessageQueue implements MessageQueue {

    private final ObjectMapper objectMapper;
    private final Path queueFile;
    private final ReentrantLock lock = new ReentrantLock();
    private final Deque<QueueMessage> messages = new ArrayDeque<>();

    public PersistentMessageQueue(ObjectMapper objectMapper,
                                  @Value("${queue.file:queue/messages.json}") String queueFilePath) {
        this.objectMapper = objectMapper;
        this.queueFile = Path.of(queueFilePath);
        load();
    }

    @Override
    public void enqueue(QueueMessage message) {
        lock.lock();
        try {
            messages.addLast(message);
            persist();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public QueueMessage poll() {
        lock.lock();
        try {
            QueueMessage message = messages.pollFirst();
            if (message != null) {
                persist();
            }
            return message;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return messages.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<QueueMessage> peekAll() {
        lock.lock();
        try {
            return new ArrayList<>(messages);
        } finally {
            lock.unlock();
        }
    }

    private void load() {
        try {
            if (Files.exists(queueFile)) {
                String content = Files.readString(queueFile);
                if (!content.isBlank()) {
                    List<QueueMessage> loaded = objectMapper.readValue(content, new TypeReference<>() {});
                    messages.addAll(loaded);
                }
            }
        } catch (IOException e) {
            System.err.println("[PersistentMessageQueue] Falha ao carregar fila: " + e.getMessage());
        }
    }

    private void persist() {
        try {
            Files.createDirectories(queueFile.getParent());
            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new ArrayList<>(messages));
            Files.writeString(queueFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("[PersistentMessageQueue] Falha ao salvar fila: " + e.getMessage());
        }
    }
}

package br.ufc.ds.trabalho3.api.queue;

import br.ufc.ds.trabalho2.app.InvestidorServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MessageQueueConsumer {

    private final MessageQueue messageQueue;
    private final InvestidorServiceImpl service;
    private final boolean processingEnabled;

    public MessageQueueConsumer(MessageQueue messageQueue,
                                InvestidorServiceImpl service,
                                @Value("${queue.processing.enabled:true}") boolean processingEnabled) {
        this.messageQueue = messageQueue;
        this.service = service;
        this.processingEnabled = processingEnabled;
    }

    @Scheduled(fixedDelayString = "${queue.processing.delay-ms:2000}")
    public void processPendingMessages() {
        if (!processingEnabled) {
            return;
        }

        QueueMessage message = messageQueue.poll();
        while (message != null) {
            try {
                processMessage(message);
            } catch (Exception exception) {
                System.err.println("[MessageQueueConsumer] Erro ao processar mensagem: " + exception.getMessage());
            }
            message = messageQueue.poll();
        }
    }

    private void processMessage(QueueMessage message) {
        if (message instanceof CreateInvestorMessage createInvestorMessage) {
            service.criarInvestidor(
                    createInvestorMessage.investidorId(),
                    createInvestorMessage.nome(),
                    createInvestorMessage.cpf(),
                    createInvestorMessage.email(),
                    createInvestorMessage.telefone());
            System.out.println("[MessageQueueConsumer] Processou CreateInvestorMessage: " + createInvestorMessage.investidorId());
            return;
        }

        if (message instanceof AddBalanceMessage addBalanceMessage) {
            service.adicionarSaldoCarteira(addBalanceMessage.investidorId(), addBalanceMessage.valor());
            System.out.println("[MessageQueueConsumer] Processou AddBalanceMessage: " + addBalanceMessage.investidorId());
            return;
        }

        if (message instanceof CreateOrderMessage createOrderMessage) {
            String orderId = createOrderMessage.ordemId().startsWith(createOrderMessage.investidorId())
                    ? createOrderMessage.ordemId()
                    : createOrderMessage.investidorId() + "_" + createOrderMessage.ordemId();
            service.criarOrdem(
                    orderId,
                    createOrderMessage.tipo(),
                    createOrderMessage.ticker(),
                    createOrderMessage.quantidade(),
                    createOrderMessage.precoUnitario());
            System.out.println("[MessageQueueConsumer] Processou CreateOrderMessage: " + orderId);
        }
    }
}

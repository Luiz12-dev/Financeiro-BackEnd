package projeto.financeiro.financeiro.domain.transaction.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import projeto.financeiro.financeiro.config.KafkaTopicConfig;
import projeto.financeiro.financeiro.domain.transaction.model.Transaction;

import java.time.OffsetDateTime;

@Service
public class TransactionEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransactionEvent(Transaction transaction) {
        TransactionEventDTO event = new TransactionEventDTO(
                transaction.getId(),
                transaction.getIdempotencyKey(),
                transaction.getSourceWallet() != null ? transaction.getSourceWallet().getId() : null,
                transaction.getDestinationWallet() != null ? transaction.getDestinationWallet().getId() : null,
                transaction.getAmount(),
                transaction.getTransactionType().name(),
                transaction.getStatus().name(),
                OffsetDateTime.now()
        );

        log.info("Publicando evento no Kafka [tópico: {}, transactionId: {}]", KafkaTopicConfig.TRANSACTIONS_TOPIC, transaction.getId());
        kafkaTemplate.send(KafkaTopicConfig.TRANSACTIONS_TOPIC, transaction.getId().toString(), event);
    }
}

package projeto.financeiro.financeiro.domain.transaction.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import projeto.financeiro.financeiro.config.KafkaTopicConfig;

@Service
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    @KafkaListener(topics = KafkaTopicConfig.TRANSACTIONS_TOPIC, groupId = "finwallet-audit-group")
    public void consumeTransactionEvent(TransactionEventDTO event) {
        log.info("Evento transacional consumido com sucesso! [ID: {}, Tipo: {}, Status: {}, Valor: {}]",
                event.transactionId(), event.transactionType(), event.status(), event.amount());
        
        // Aqui em um cenário real processamos logs de auditoria no Elasticsearch,
        // disparamos notificações Push/SMS para os titulares das carteiras, ou alimentamos o Outbox.
    }
}

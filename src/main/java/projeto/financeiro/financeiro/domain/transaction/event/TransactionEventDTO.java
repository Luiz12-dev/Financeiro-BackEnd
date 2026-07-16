package projeto.financeiro.financeiro.domain.transaction.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionEventDTO(
    UUID transactionId,
    String idempotencyKey,
    UUID sourceWalletId,
    UUID destinationWalletId,
    BigDecimal amount,
    String transactionType,
    String status,
    OffsetDateTime timestamp
) {}

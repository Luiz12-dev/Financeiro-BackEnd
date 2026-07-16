package projeto.financeiro.financeiro.domain.transaction.dto;

import projeto.financeiro.financeiro.domain.transaction.model.TransactionStatus;
import projeto.financeiro.financeiro.domain.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionResponseDTO(
    UUID id,
    String idempotencyKey,
    UUID sourceWalletId,
    UUID destinationWalletId,
    BigDecimal amount,
    TransactionType transactionType,
    TransactionStatus status,
    String description,
    String errorReason,
    OffsetDateTime createdAt,
    OffsetDateTime completedAt
) {}

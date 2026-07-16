package projeto.financeiro.financeiro.domain.wallet.dto;

import projeto.financeiro.financeiro.domain.wallet.model.WalletStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletResponseDTO(
    UUID id,
    UUID userId,
    String currency,
    BigDecimal balance,
    WalletStatus status,
    OffsetDateTime updatedAt
) {}

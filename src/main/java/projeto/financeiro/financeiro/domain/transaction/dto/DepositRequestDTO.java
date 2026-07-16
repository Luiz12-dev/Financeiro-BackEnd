package projeto.financeiro.financeiro.domain.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositRequestDTO(
    @NotNull(message = "O ID da carteira de destino é obrigatório")
    UUID destinationWalletId,

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor do depósito deve ser estritamente positivo")
    BigDecimal amount,

    String description
) {}

package projeto.financeiro.financeiro.domain.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDTO(
    @NotNull(message = "O ID da carteira de origem é obrigatório")
    UUID sourceWalletId,

    @NotNull(message = "O ID da carteira de destino é obrigatório")
    UUID destinationWalletId,

    @NotNull(message = "O valor da transferência é obrigatório")
    @Positive(message = "O valor da transferência deve ser estritamente positivo")
    BigDecimal amount,

    String description
) {}

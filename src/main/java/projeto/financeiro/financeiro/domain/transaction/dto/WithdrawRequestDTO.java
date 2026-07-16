package projeto.financeiro.financeiro.domain.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawRequestDTO(
    @NotNull(message = "O ID da carteira de origem é obrigatório")
    UUID sourceWalletId,

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor do saque deve ser estritamente positivo")
    BigDecimal amount,

    String description
) {}

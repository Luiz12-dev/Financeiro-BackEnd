package projeto.financeiro.financeiro.domain.auth.dto;

import java.util.UUID;

public record AuthResponseDTO(
    String token,
    UUID userId,
    String email,
    String fullName
) {}

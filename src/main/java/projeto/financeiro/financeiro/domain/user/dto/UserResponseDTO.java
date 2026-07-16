package projeto.financeiro.financeiro.domain.user.dto;

import projeto.financeiro.financeiro.domain.user.model.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDTO(
    UUID id,
    String fullName,
    String email,
    String document,
    UserStatus status,
    OffsetDateTime createdAt
) {}

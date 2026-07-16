package projeto.financeiro.financeiro.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequestDTO(
    @NotBlank(message = "O nome completo é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    String fullName,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    String email,

    @NotBlank(message = "O CPF/CNPJ é obrigatório")
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "O documento deve conter apenas números e possuir 11 (CPF) ou 14 (CNPJ) dígitos")
    String document,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    String password
) {}

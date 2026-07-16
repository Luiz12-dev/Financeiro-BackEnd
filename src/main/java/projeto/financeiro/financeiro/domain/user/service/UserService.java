package projeto.financeiro.financeiro.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.financeiro.financeiro.domain.user.dto.UserCreateRequestDTO;
import projeto.financeiro.financeiro.domain.user.dto.UserResponseDTO;
import projeto.financeiro.financeiro.domain.user.model.User;
import projeto.financeiro.financeiro.domain.user.model.UserStatus;
import projeto.financeiro.financeiro.domain.user.repository.UserRepository;
import projeto.financeiro.financeiro.domain.wallet.service.WalletService;
import projeto.financeiro.financeiro.shared.exception.DuplicateResourceException;
import projeto.financeiro.financeiro.shared.exception.ResourceNotFoundException;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, WalletService walletService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateRequestDTO userDto) {
        validateDuplicatedUser(userDto.email(), userDto.document());

        User user = User.builder()
                .fullName(userDto.fullName())
                .email(userDto.email())
                .document(userDto.document())
                .passwordHash(passwordEncoder.encode(userDto.password()))
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        // Criação transacional da carteira inicial padrão (BRL)
        walletService.createDefaultWallet(savedUser, "BRL");

        return toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + userId));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserDTOById(UUID userId) {
        User user = getUserById(userId);
        return toDTO(user);
    }

    private void validateDuplicatedUser(String email, String document) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("Já existe um usuário cadastrado com este e-mail: " + email);
        }
        if (userRepository.findByDocument(document).isPresent()) {
            throw new DuplicateResourceException("Já existe um usuário cadastrado com este CPF/CNPJ: " + document);
        }
    }

    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getDocument(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}

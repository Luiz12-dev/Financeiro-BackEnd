package projeto.financeiro.financeiro.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import projeto.financeiro.financeiro.domain.auth.dto.AuthResponseDTO;
import projeto.financeiro.financeiro.domain.auth.dto.LoginRequestDTO;
import projeto.financeiro.financeiro.domain.user.model.User;
import projeto.financeiro.financeiro.domain.user.repository.UserRepository;
import projeto.financeiro.financeiro.shared.exception.ResourceNotFoundException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        Authentication auth = authenticationManager.authenticate(usernamePassword);

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + dto.email()));

        String token = jwtTokenService.generateToken(user);

        return new AuthResponseDTO(token, user.getId(), user.getEmail(), user.getFullName());
    }
}

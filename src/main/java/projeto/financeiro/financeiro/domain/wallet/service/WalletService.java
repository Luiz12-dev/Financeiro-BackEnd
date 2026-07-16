package projeto.financeiro.financeiro.domain.wallet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.financeiro.financeiro.domain.user.model.User;
import projeto.financeiro.financeiro.domain.wallet.dto.WalletResponseDTO;
import projeto.financeiro.financeiro.domain.wallet.model.Wallet;
import projeto.financeiro.financeiro.domain.wallet.model.WalletStatus;
import projeto.financeiro.financeiro.domain.wallet.repository.WalletRepository;
import projeto.financeiro.financeiro.shared.exception.InsufficientBalanceException;
import projeto.financeiro.financeiro.shared.exception.ResourceNotFoundException;
import projeto.financeiro.financeiro.shared.exception.WalletBlockedException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Wallet createDefaultWallet(User user, String currency) {
        Wallet wallet = Wallet.builder()
                .user(user)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .build();
        return walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com o ID: " + walletId));
    }

    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletDTOById(UUID walletId) {
        Wallet wallet = getWalletById(walletId);
        return toDTO(wallet);
    }

    @Transactional
    public Wallet getWalletForUpdate(UUID userId, String currency) {
        return walletRepository.findByUserIdAndCurrencyForUpdate(userId, currency)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carteira com moeda " + currency + " não encontrada para o usuário " + userId));
    }

    @Transactional
    public void credit(Wallet wallet, BigDecimal amount) {
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void debit(Wallet wallet, BigDecimal amount) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletBlockedException("Carteira bloqueada para operações de débito: " + wallet.getId());
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente na carteira: " + wallet.getId());
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    public WalletResponseDTO toDTO(Wallet wallet) {
        return new WalletResponseDTO(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getCurrency(),
                wallet.getBalance(),
                wallet.getStatus(),
                wallet.getUpdatedAt()
        );
    }
}

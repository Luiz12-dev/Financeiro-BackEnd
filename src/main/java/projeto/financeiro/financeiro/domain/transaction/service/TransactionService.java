package projeto.financeiro.financeiro.domain.transaction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.financeiro.financeiro.domain.transaction.dto.DepositRequestDTO;
import projeto.financeiro.financeiro.domain.transaction.dto.TransactionResponseDTO;
import projeto.financeiro.financeiro.domain.transaction.dto.TransferRequestDTO;
import projeto.financeiro.financeiro.domain.transaction.dto.WithdrawRequestDTO;
import projeto.financeiro.financeiro.domain.transaction.event.TransactionEventProducer;
import projeto.financeiro.financeiro.domain.transaction.model.Transaction;
import projeto.financeiro.financeiro.domain.transaction.model.TransactionStatus;
import projeto.financeiro.financeiro.domain.transaction.model.TransactionType;
import projeto.financeiro.financeiro.domain.transaction.repository.TransactionRepository;
import projeto.financeiro.financeiro.domain.wallet.model.Wallet;
import projeto.financeiro.financeiro.domain.wallet.service.WalletService;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final IdempotencyService idempotencyService;
    private final TransactionEventProducer eventProducer;

    public TransactionService(TransactionRepository transactionRepository,
                              WalletService walletService,
                              IdempotencyService idempotencyService,
                              TransactionEventProducer eventProducer) {
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.idempotencyService = idempotencyService;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public TransactionResponseDTO deposit(DepositRequestDTO dto, String idempotencyKey) {
        // 1. Verificação ultra-rápida no Redis (Distributed Lock de Idempotência)
        idempotencyService.tryLock(idempotencyKey);

        try {
            // 2. Barreira persistente no banco de dados (caso retentativa válida)
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                idempotencyService.markCompleted(idempotencyKey);
                return toDTO(existing.get());
            }

            Wallet destination = walletService.getWalletById(dto.destinationWalletId());

            Transaction transaction = Transaction.builder()
                    .idempotencyKey(idempotencyKey)
                    .destinationWallet(destination)
                    .amount(dto.amount())
                    .transactionType(TransactionType.DEPOSIT)
                    .status(TransactionStatus.PENDING)
                    .description(dto.description())
                    .build();
            transactionRepository.save(transaction);

            try {
                walletService.credit(destination, dto.amount());
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(OffsetDateTime.now());

                // Disparo do Evento Kafka assíncrono para auditoria
                eventProducer.sendTransactionEvent(transaction);
                idempotencyService.markCompleted(idempotencyKey);
            } catch (Exception e) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorReason(e.getMessage());
                idempotencyService.unlock(idempotencyKey);
                throw e;
            } finally {
                transactionRepository.save(transaction);
            }

            return toDTO(transaction);
        } catch (Exception e) {
            idempotencyService.unlock(idempotencyKey);
            throw e;
        }
    }

    @Transactional
    public TransactionResponseDTO withdraw(WithdrawRequestDTO dto, String idempotencyKey) {
        idempotencyService.tryLock(idempotencyKey);

        try {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                idempotencyService.markCompleted(idempotencyKey);
                return toDTO(existing.get());
            }

            Wallet source = walletService.getWalletById(dto.sourceWalletId());
            source = walletService.getWalletForUpdate(source.getUser().getId(), source.getCurrency());

            Transaction transaction = Transaction.builder()
                    .idempotencyKey(idempotencyKey)
                    .sourceWallet(source)
                    .amount(dto.amount())
                    .transactionType(TransactionType.WITHDRAWAL)
                    .status(TransactionStatus.PENDING)
                    .description(dto.description())
                    .build();
            transactionRepository.save(transaction);

            try {
                walletService.debit(source, dto.amount());
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(OffsetDateTime.now());

                eventProducer.sendTransactionEvent(transaction);
                idempotencyService.markCompleted(idempotencyKey);
            } catch (Exception e) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorReason(e.getMessage());
                idempotencyService.unlock(idempotencyKey);
                throw e;
            } finally {
                transactionRepository.save(transaction);
            }

            return toDTO(transaction);
        } catch (Exception e) {
            idempotencyService.unlock(idempotencyKey);
            throw e;
        }
    }

    @Transactional
    public TransactionResponseDTO transfer(TransferRequestDTO dto, String idempotencyKey) {
        idempotencyService.tryLock(idempotencyKey);

        try {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                idempotencyService.markCompleted(idempotencyKey);
                return toDTO(existing.get());
            }

            if (dto.sourceWalletId().equals(dto.destinationWalletId())) {
                throw new IllegalArgumentException("A carteira de origem não pode ser igual à de destino");
            }

            Wallet source = walletService.getWalletById(dto.sourceWalletId());
            Wallet destination = walletService.getWalletById(dto.destinationWalletId());

            if (!source.getCurrency().equals(destination.getCurrency())) {
                throw new IllegalArgumentException("Transferências entre moedas diferentes exigem conversão cambial");
            }

            source = walletService.getWalletForUpdate(source.getUser().getId(), source.getCurrency());

            Transaction transaction = Transaction.builder()
                    .idempotencyKey(idempotencyKey)
                    .sourceWallet(source)
                    .destinationWallet(destination)
                    .amount(dto.amount())
                    .transactionType(TransactionType.TRANSFER)
                    .status(TransactionStatus.PENDING)
                    .description(dto.description())
                    .build();
            transactionRepository.save(transaction);

            try {
                walletService.debit(source, dto.amount());
                walletService.credit(destination, dto.amount());

                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(OffsetDateTime.now());

                eventProducer.sendTransactionEvent(transaction);
                idempotencyService.markCompleted(idempotencyKey);
            } catch (Exception e) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorReason(e.getMessage());
                idempotencyService.unlock(idempotencyKey);
                throw e;
            } finally {
                transactionRepository.save(transaction);
            }

            return toDTO(transaction);
        } catch (Exception e) {
            idempotencyService.unlock(idempotencyKey);
            throw e;
        }
    }

    private TransactionResponseDTO toDTO(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getIdempotencyKey(),
                transaction.getSourceWallet() != null ? transaction.getSourceWallet().getId() : null,
                transaction.getDestinationWallet() != null ? transaction.getDestinationWallet().getId() : null,
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getErrorReason(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}

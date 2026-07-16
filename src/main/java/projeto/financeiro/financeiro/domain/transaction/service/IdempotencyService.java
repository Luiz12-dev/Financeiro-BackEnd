package projeto.financeiro.financeiro.domain.transaction.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import projeto.financeiro.financeiro.shared.exception.IdempotencyConflictException;

import java.time.Duration;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "IDEMPOTENCY:";
    private static final Duration LOCK_TTL = Duration.ofMinutes(15);

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Tenta registrar a chave no Redis. Se a chave já existir no Redis com status IN_PROGRESS,
     * lança IdempotencyConflictException evitando que threads concorrentes tentem processar ao mesmo tempo.
     */
    public void tryLock(String idempotencyKey) {
        String redisKey = KEY_PREFIX + idempotencyKey;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(redisKey, "IN_PROGRESS", LOCK_TTL);
        if (Boolean.FALSE.equals(acquired)) {
            throw new IdempotencyConflictException(
                    "Transação em andamento ou já processada com esta chave de idempotência: " + idempotencyKey);
        }
    }

    /**
     * Ao finalizar com sucesso, marca a chave como COMPLETED com TTL de 24h no Redis.
     */
    public void markCompleted(String idempotencyKey) {
        String redisKey = KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(redisKey, "COMPLETED", Duration.ofHours(24));
    }

    /**
     * Em caso de falha transitória onde queremos permitir que o cliente tente novamente,
     * remove a chave do Redis.
     */
    public void unlock(String idempotencyKey) {
        String redisKey = KEY_PREFIX + idempotencyKey;
        redisTemplate.delete(redisKey);
    }
}

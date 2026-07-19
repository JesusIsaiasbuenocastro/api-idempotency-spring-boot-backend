package com.jesus.portafolio.idempotency.infrastructure.adapter.out.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesus.portafolio.idempotency.application.exception.IdempotencyConflictException;
import com.jesus.portafolio.idempotency.application.port.out.IdempotencyDecision;
import com.jesus.portafolio.idempotency.application.port.out.IdempotencyStoragePort;
import com.jesus.portafolio.idempotency.application.port.out.IdempotentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisIdempotencyStorageAdapter implements IdempotencyStoragePort {
    private static final Logger log = LoggerFactory.getLogger(RedisIdempotencyStorageAdapter.class);

    private static final String KEY_PREFIX = "idempotency:";
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyStorageAdapter(ObjectMapper objectMapper, StringRedisTemplate redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public IdempotencyDecision begin(String key, String requestHash, String operationName, long ttlSeconds) {
        String redisKey = KEY_PREFIX + key;

        String reservationJson = write(RedisIdempotencyRecord.inProgress(requestHash));

        Boolean reserved = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, reservationJson, Duration.ofSeconds(ttlSeconds));

        if (Boolean.TRUE.equals(reserved)) {
            return IdempotencyDecision.proceed();
        }

        String existingJson = redisTemplate.opsForValue().get(redisKey);

        if (existingJson == null) {
            // Caso raro: la clave expiró justo entre el SETNX fallido y este GET.
            Boolean retryReserved = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, reservationJson, Duration.ofSeconds(ttlSeconds));
            if (Boolean.TRUE.equals(retryReserved)) {
                return IdempotencyDecision.proceed();
            }
            existingJson = redisTemplate.opsForValue().get(redisKey);
        }

        RedisIdempotencyRecord existing = read(existingJson);

        if (existing.isInProgress()) {
            throw new IdempotencyConflictException(
                   "IDEMPOTENCY_KEY_IN_PROGRESS", "Ya existe una solicitud en curso con esta Idempotency-Key. Reintenta en unos segundos.");
        }

        if (existing.isCompleted()) {
            if (existing.requestHash().equals(requestHash)) {
                log.info("Idempotency-Key '{}' repetida con el mismo payload: devolviendo respuesta cacheada desde Redis", key);
                return IdempotencyDecision.cached(new IdempotentPayload(
                        existing.httpStatus(), existing.body(), existing.contentType()));
            }
            throw new IdempotencyConflictException(
                    "IDEMPOTENCY_KEY_REUSED_DIFFERENT_PAYLOAD", "Esta Idempotency-Key ya se usó para una solicitud distinta. Usa una clave nueva por cada operación lógica.");

        }

        return IdempotencyDecision.proceed();
    }

    @Override
    public void complete(String key, IdempotentPayload payload, long ttlSeconds) {
        String redisKey = KEY_PREFIX + key;
        String existingJson = redisTemplate.opsForValue().get(redisKey);

        RedisIdempotencyRecord base = existingJson != null
                ? read(existingJson)
                : RedisIdempotencyRecord.inProgress(null);

        RedisIdempotencyRecord completed = base.asCompleted(payload.httpStatus(), payload.body(), payload.contentType());

        redisTemplate.opsForValue().set(redisKey, write(completed), Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void fail(String key) {
        redisTemplate.delete(KEY_PREFIX + key);
    }

    private String write(RedisIdempotencyRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el registro de idempotencia", e);
        }
    }

    private RedisIdempotencyRecord read(String json) {
        try {
            return objectMapper.readValue(json, RedisIdempotencyRecord.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo deserializar el registro de idempotencia", e);
        }
    }
}

package com.jesus.portafolio.idempotency.application.port.out;


public interface IdempotencyStoragePort {
    IdempotencyDecision begin(String key, String requestHash, String operationName, long ttlSeconds);

    void complete(String key, IdempotentPayload payload, long ttlSeconds);

    void fail(String key);
}

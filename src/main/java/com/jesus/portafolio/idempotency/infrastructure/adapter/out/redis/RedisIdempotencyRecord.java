package com.jesus.portafolio.idempotency.infrastructure.adapter.out.redis;

public record RedisIdempotencyRecord(
        String status, // "IN_PROGRESS" | "COMPLETED"
        String requestHash,
        Integer httpStatus,
        String body,
        String contentType
) {
    static RedisIdempotencyRecord inProgress(String requestHash) {
        return new RedisIdempotencyRecord("IN_PROGRESS", requestHash, null, null, null);
    }

    RedisIdempotencyRecord asCompleted(Integer httpStatus, String body, String contentType) {
        return new RedisIdempotencyRecord("COMPLETED", this.requestHash, httpStatus, body, contentType);
    }

    boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}

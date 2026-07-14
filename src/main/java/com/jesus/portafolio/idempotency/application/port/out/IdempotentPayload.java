package com.jesus.portafolio.idempotency.application.port.out;

public record IdempotentPayload(Integer httpStatus, String body, String contentType) {
    public static IdempotentPayload ofJson(String json) {
        return new IdempotentPayload(null, json, "application/json");
    }
}

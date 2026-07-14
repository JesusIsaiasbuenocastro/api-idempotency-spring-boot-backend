package com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.dto;

import java.time.Instant;

public record PaymentResponse(
        String transactionId,
        String accountId,
        Double amount,
        String currency,
        String status,
        Instant processedAt
) {
}


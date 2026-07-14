package com.jesus.portafolio.idempotency.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Payment (
        String transactionId,
        String accountId,
        Double amount,
        String currency,
        PaymentStatus status,
        Instant processedAt
){}

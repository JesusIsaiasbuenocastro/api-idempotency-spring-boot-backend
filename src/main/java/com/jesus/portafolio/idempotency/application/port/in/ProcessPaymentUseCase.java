package com.jesus.portafolio.idempotency.application.port.in;

import com.jesus.portafolio.idempotency.domain.model.Payment;

public interface ProcessPaymentUseCase {

    record PaymentCommand(String accountId, Double amount, String currency, String requestId) {
    }

    Payment save(PaymentCommand paymentCommand);

}

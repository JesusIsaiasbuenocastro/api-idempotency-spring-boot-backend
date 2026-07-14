package com.jesus.portafolio.idempotency.application.port.in;

public interface ProcessPaymentUseCase {

    record PaymentCommand(String accountId, Double amount, String currency, String requestId) {
    }

    <T> T save(PaymentCommand paymentCommand);

}

package com.jesus.portafolio.idempotency.application.command;

public record PaymentCommand(String accountId, Double amount, String currency, String requestId)  {
}

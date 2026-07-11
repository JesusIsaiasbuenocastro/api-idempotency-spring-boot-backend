package com.jesus.portafolio.idempotency.application.port.in;

public interface ProcessPaymentUseCase {
    boolean save(String paymentModel);

}

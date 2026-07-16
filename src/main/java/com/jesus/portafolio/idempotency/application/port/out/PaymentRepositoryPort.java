package com.jesus.portafolio.idempotency.application.port.out;

import com.jesus.portafolio.idempotency.domain.model.Payment;

public interface PaymentRepositoryPort {
    Payment save(Payment payment);

}

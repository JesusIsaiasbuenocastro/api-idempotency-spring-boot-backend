package com.jesus.portafolio.idempotency.application.port.in;

import com.jesus.portafolio.idempotency.application.command.PaymentCommand;
import com.jesus.portafolio.idempotency.domain.model.Payment;

public interface ProcessPaymentAspectUseCase {

    Payment save(PaymentCommand paymentCommand);

    Payment saveUnsafe(PaymentCommand paymentCommand);

    // Solo para fines demostrativos (contador de cobros reales que llegan a Postgres)
    int countProcessedPayments();

}

package com.jesus.portafolio.idempotency.application.service;

import com.jesus.portafolio.idempotency.application.port.in.ProcessPaymentUseCase;
import com.jesus.portafolio.idempotency.application.port.out.PaymentRepositoryPort;
import com.jesus.portafolio.idempotency.domain.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProcessPaymentServiceInterceptor implements ProcessPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentServiceInterceptor.class);

    private final PaymentRepositoryPort paymentRepositoryPort;

    public ProcessPaymentServiceInterceptor(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }


    @Override
    public Payment save(PaymentCommand paymentCommand) {
        log.info(">>> Procesando pago REAL para la cuenta {}", paymentCommand.accountId());

        Payment payment = Payment.completed(
                UUID.randomUUID().toString(), paymentCommand.accountId(), paymentCommand.amount(), paymentCommand.currency());

        return paymentRepositoryPort.save(payment);

    }

    @Override
    public int countProcessedPayments() {
        return paymentRepositoryPort.countProcessed();

    }
}

package com.jesus.portafolio.idempotency.application.service;

import com.jesus.portafolio.idempotency.application.annotation.Idempotency;
import com.jesus.portafolio.idempotency.application.annotation.IdempotencyOperation;
import com.jesus.portafolio.idempotency.application.command.PaymentCommand;
import com.jesus.portafolio.idempotency.application.port.in.ProcessPaymentAspectUseCase;
import com.jesus.portafolio.idempotency.application.port.out.PaymentRepositoryPort;
import com.jesus.portafolio.idempotency.domain.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProcessPaymentServiceAspect implements ProcessPaymentAspectUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentServiceAspect.class);

    private final PaymentRepositoryPort paymentRepositoryPort;

    public ProcessPaymentServiceAspect(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }


    @Override
    @IdempotencyOperation(ttlSeconds=86400)
    public Payment save(PaymentCommand paymentCommand) {
        log.info(">Procesando pago real (aspect)< para la cuenta {}",paymentCommand.accountId());
        Payment payment = Payment.completed(
                UUID.randomUUID().toString(), paymentCommand.accountId(), paymentCommand.amount(), paymentCommand.currency());
        return paymentRepositoryPort.save(payment);
    }

    @Override
    public Payment saveUnsafe(PaymentCommand paymentCommand) {
        log.info(">Procesando pago real (aspect)< para la cuenta {}",paymentCommand.accountId());
        Payment payment = Payment.completed(
                UUID.randomUUID().toString(), paymentCommand.accountId(), paymentCommand.amount(), paymentCommand.currency());
        return paymentRepositoryPort.save(payment);
    }
    @Override
    public int countProcessedPayments() {
        return paymentRepositoryPort.countProcessed();
    }
}

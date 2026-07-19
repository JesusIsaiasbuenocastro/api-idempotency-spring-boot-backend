package com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.mapper;

import com.jesus.portafolio.idempotency.application.command.PaymentCommand;
import com.jesus.portafolio.idempotency.domain.model.Payment;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.dto.PaymentRequest;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.dto.PaymentResponse;

public final class PaymentWebMapper {

    private PaymentWebMapper() {
    }

    public static PaymentCommand toCommand(PaymentRequest request){
        return new PaymentCommand(request.accountId(), request.amout(),request.currency(), request.requestId());
    }

    public static PaymentResponse toResponse(Payment payment){
        return new PaymentResponse(
                payment.transactionId() == null ? "" : payment.transactionId(),
                payment.accountId(),
                payment.amount(),
                payment.currency(),
                payment.status().name(),
                payment.processedAt()
        );
    }
}

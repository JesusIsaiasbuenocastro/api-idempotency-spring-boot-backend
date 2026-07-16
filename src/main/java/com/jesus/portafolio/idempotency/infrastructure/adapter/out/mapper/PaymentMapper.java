package com.jesus.portafolio.idempotency.infrastructure.adapter.out.mapper;

import com.jesus.portafolio.idempotency.domain.model.Payment;
import com.jesus.portafolio.idempotency.infrastructure.adapter.out.persistence.postgresql.PaymentJpaEntity;

public class PaymentMapper {

    public static PaymentJpaEntity toEntity(Payment payment){

        PaymentJpaEntity paymentJpaEntity = new PaymentJpaEntity();
        paymentJpaEntity.setTransactionId(payment.transactionId());
        paymentJpaEntity.setAccountId(payment.accountId());
        paymentJpaEntity.setAmount(payment.amount());
        paymentJpaEntity.setCurrency(payment.currency());
        paymentJpaEntity.setStatus(payment.status());
        paymentJpaEntity.setProcessedAt(payment.processedAt());

      return paymentJpaEntity ;
    }

    public static Payment toDomain(PaymentJpaEntity paymentJpaEntity){
        return new Payment(
                paymentJpaEntity.getTransactionId(),
                paymentJpaEntity.getAccountId(),
                paymentJpaEntity.getAmount(),
                paymentJpaEntity.getCurrency(),
                paymentJpaEntity.getStatus(),
                paymentJpaEntity.getProcessedAt()
        );
    }
}

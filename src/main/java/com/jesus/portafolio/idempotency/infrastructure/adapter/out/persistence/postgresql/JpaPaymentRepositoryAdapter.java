package com.jesus.portafolio.idempotency.infrastructure.adapter.out.persistence.postgresql;

import com.jesus.portafolio.idempotency.application.port.out.PaymentRepositoryPort;
import com.jesus.portafolio.idempotency.domain.model.Payment;
import com.jesus.portafolio.idempotency.infrastructure.adapter.out.mapper.PaymentMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JpaPaymentRepositoryAdapter implements PaymentRepositoryPort {
    private final PaymentJpaRepository paymentJpaRepository;

    public JpaPaymentRepositoryAdapter(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    public Payment save(Payment payment){
        //PaymentJpaEntity entity = mapper.toEntity(payment);

        Payment paymenttest = Payment.completed("tx1", "acc1", 100.0, "USD");
        PaymentJpaEntity entity = PaymentMapper.toEntity(payment);
        return PaymentMapper.toDomain(paymentJpaRepository.save(entity));
    }
}

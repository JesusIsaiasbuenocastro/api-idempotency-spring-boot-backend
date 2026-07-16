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
        return PaymentMapper.toDomain(paymentJpaRepository.save(PaymentMapper.toEntity(payment)));
    }

    @Override
    public int countProcessed() {
        return (int) paymentJpaRepository.count();
    }
}

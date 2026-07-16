package com.jesus.portafolio.idempotency.infrastructure.adapter.out.persistence.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity,Long> {
}

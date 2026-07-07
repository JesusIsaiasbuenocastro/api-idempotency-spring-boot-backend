package com.jesus.portafolio.idempotency.service;

public interface IdempotencyService {
    boolean save(String paymentModel);
}

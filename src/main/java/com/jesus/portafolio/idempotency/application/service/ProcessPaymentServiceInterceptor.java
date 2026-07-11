package com.jesus.portafolio.idempotency.application.service;

import com.jesus.portafolio.idempotency.application.port.in.ProcessPaymentUseCase;
import org.springframework.stereotype.Service;

@Service
public class ProcessPaymentServiceInterceptor implements ProcessPaymentUseCase {

    @Override
    public boolean save(String paymentModel) {
        return false;
    }
}

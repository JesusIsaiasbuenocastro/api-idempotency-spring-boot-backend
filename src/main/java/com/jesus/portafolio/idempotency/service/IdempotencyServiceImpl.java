package com.jesus.portafolio.idempotency.service;

import org.springframework.stereotype.Service;

@Service
public class IdempotencyServiceImpl implements IdempotencyService{

    @Override
    public boolean save(String paymentModel) {
        //validación del request 
        return true;
    }

}

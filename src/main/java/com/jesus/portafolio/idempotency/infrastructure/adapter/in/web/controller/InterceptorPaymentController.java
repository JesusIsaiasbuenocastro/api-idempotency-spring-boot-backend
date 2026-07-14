package com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.controller;

import com.jesus.portafolio.idempotency.application.port.in.ProcessPaymentUseCase;
import com.jesus.portafolio.idempotency.domain.model.Payment;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.dto.PaymentRequest;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.dto.PaymentResponse;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.mapper.PaymentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jesus.portafolio.idempotency.application.annotation.Idempotency;



@RestController
@RequestMapping("/api/v1/payment")
public class InterceptorPaymentController {

    public final ProcessPaymentUseCase processPaymentUseCase;

    public InterceptorPaymentController(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }


    @PostMapping
    @Idempotency(ttlSeconds=120)
    public ResponseEntity<PaymentResponse> postMethodName(@Valid @RequestBody PaymentRequest request) {
        var payment = processPaymentUseCase.save(PaymentWebMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentWebMapper.toResponse((Payment) payment));
    }

     @GetMapping
     public String getMethodName() {
         return "Consultar todos";
     }
      

}

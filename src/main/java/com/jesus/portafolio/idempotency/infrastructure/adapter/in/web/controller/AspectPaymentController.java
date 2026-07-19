package com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.controller;

import com.jesus.portafolio.idempotency.application.port.in.ProcessPaymentAspectUseCase;
import com.jesus.portafolio.idempotency.application.service.ProcessPaymentServiceAspect;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.dto.PaymentRequest;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.dto.PaymentResponse;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.mapper.PaymentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/payment")
public class AspectPaymentController {

    private final ProcessPaymentAspectUseCase processPaymentAspectUseCase;

    public AspectPaymentController(ProcessPaymentServiceAspect processPaymentServiceAspect) {
        this.processPaymentAspectUseCase = processPaymentServiceAspect;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> postPayment (@Valid @RequestBody PaymentRequest request){
        var payment = processPaymentAspectUseCase.save(PaymentWebMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentWebMapper.toResponse(payment));
    }

    @PostMapping("/unsafe")
    public ResponseEntity<PaymentResponse> createPaymentUnsafe(@Valid @RequestBody PaymentRequest request) {
        var payment = processPaymentAspectUseCase.saveUnsafe(PaymentWebMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentWebMapper.toResponse(payment));
    }

    @GetMapping("/executions-count")
    public ResponseEntity<Integer> getExecutionsCount() {
        return ResponseEntity.ok(processPaymentAspectUseCase.countProcessedPayments());
    }
}

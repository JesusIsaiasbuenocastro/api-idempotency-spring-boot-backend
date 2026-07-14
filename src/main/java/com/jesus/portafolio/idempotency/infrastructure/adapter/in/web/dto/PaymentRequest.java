package com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest (
     @NotBlank
     String accountId,
     @NotNull
     @DecimalMin("0.01")
     Double amout,
     @NotBlank
     String currency,
     //esto es solo para la implentacion del aspect
     String requestId
){}

package com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    public String orderId;
    public BigDecimal amout;
    public String currency;
}

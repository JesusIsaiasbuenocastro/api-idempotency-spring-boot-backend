package com.jesus.portafolio.idempotency.application.model;

import java.io.Serializable;

public record IdempotentRecord (
    String status, // "PROCESSING" o "COMPLETED"
    Object responseBody
) implements Serializable {}

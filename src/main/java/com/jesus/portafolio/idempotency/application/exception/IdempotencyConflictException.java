package com.jesus.portafolio.idempotency.application.exception;

public class IdempotencyConflictException extends RuntimeException{

    private final String code;

    public IdempotencyConflictException(String message, String code){
        super(message);
        this.code = code;
    }
    public String getCode() {
        return code;
    }
}

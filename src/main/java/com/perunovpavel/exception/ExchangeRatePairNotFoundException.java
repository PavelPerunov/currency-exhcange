package com.perunovpavel.exception;

public class ExchangeRatePairNotFoundException extends RuntimeException {
    public ExchangeRatePairNotFoundException(String message) {
        super(message);
    }
}

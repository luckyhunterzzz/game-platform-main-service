package com.gameplatform.mainservice.exception.exceptions;

public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) {
        super(message);
    }
}
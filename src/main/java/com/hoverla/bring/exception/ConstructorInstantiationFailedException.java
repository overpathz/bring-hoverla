package com.hoverla.bring.exception;

public class ConstructorInstantiationFailedException extends RuntimeException {
    public ConstructorInstantiationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.hoverla.bring.exception;

public class DefaultConstructorNotFoundException extends RuntimeException {

    public DefaultConstructorNotFoundException(String message) {
        super(message);
    }
}

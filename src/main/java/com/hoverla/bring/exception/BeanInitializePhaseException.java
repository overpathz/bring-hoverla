package com.hoverla.bring.exception;

public class BeanInitializePhaseException extends RuntimeException {
    public BeanInitializePhaseException(String message, Exception cause) {
        super(message, cause);
    }
}

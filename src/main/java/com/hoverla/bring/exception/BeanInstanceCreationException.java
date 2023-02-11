package com.hoverla.bring.exception;

public class BeanInstanceCreationException extends RuntimeException {
    public BeanInstanceCreationException(String message) {
        super(message);
    }
    public BeanInstanceCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}

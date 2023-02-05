package com.hoverla.bring.exception;

public class DefaultConstructorNotFoundException extends RuntimeException {
    public static final String DEFAULT_CONSTRUCTOR_NOT_FOUND_EXCEPTION = "Default constructor hasn't been found for %s";
    public DefaultConstructorNotFoundException(String message) {
        super(message);
    }
}

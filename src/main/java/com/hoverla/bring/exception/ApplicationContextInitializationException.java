package com.hoverla.bring.exception;

public class ApplicationContextInitializationException extends RuntimeException {
    public static final String APPLICATION_INITIALIZATION_EXCEPTION = "ApplicationContext initialization has failed: %s";
    public ApplicationContextInitializationException(String message) {
        super(message);
    }
}


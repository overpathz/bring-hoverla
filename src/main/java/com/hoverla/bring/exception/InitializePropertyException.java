package com.hoverla.bring.exception;

public class InitializePropertyException extends RuntimeException {
    public final static String INITIALIZE_PROPERTY_EXCEPTION = "Can't initialize property #";
    public InitializePropertyException(String message) {
        super(message);
    }
}

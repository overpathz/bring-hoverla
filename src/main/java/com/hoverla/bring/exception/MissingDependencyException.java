package com.hoverla.bring.exception;

public class MissingDependencyException extends RuntimeException {
    public MissingDependencyException(String message) {
        super(message);
    }
}

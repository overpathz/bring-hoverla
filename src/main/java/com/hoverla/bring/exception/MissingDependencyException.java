package com.hoverla.bring.exception;

public class MissingDependencyException extends RuntimeException {

    public static final String MISSING_DEPENDENCY_EXCEPTION = "Dependency of type %s and name %s hasn't been found for [%s] bean";

    public MissingDependencyException(String message) {
        super(message);
    }
}

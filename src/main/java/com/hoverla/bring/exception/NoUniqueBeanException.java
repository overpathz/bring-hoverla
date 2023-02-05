package com.hoverla.bring.exception;

public class NoUniqueBeanException extends RuntimeException {
    public final static String NO_UNIQUE_BEAN_EXCEPTION  = "There is more than one bean matching the %s type: [%s]. " +
        "Please specify a bean name!";
    public NoUniqueBeanException(String message) {
        super(message);
    }
}

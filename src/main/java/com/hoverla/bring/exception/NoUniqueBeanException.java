package com.hoverla.bring.exception;

public class NoUniqueBeanException extends RuntimeException {
    public final static String NO_UNIQUE_BEAN_EXCEPTION  = "There is more than one bean matching the %s type: [%s]. " +
        "Please specify a bean name!";

    public final static String NO_UNIQUE_PRIMARY_BEAN_EXCEPTION = "More than one 'primary' bean found among candidates: %s";
    public NoUniqueBeanException(String message) {
        super(message);
    }
}

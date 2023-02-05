package com.hoverla.bring.exception;

public class NoSuchBeanException extends RuntimeException {
    public static final String NO_SUCH_BEAN_EXCEPTION_BY_TYPE = "Bean with type %s not found";
    public static final String NO_SUCH_BEAN_EXCEPTION_BY_NAME_TYPE = "Bean with name %s and type %s not found";

    public NoSuchBeanException(String message) {
        super(message);
    }
}

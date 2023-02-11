package com.hoverla.bring.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation is used to mark the field to inject value from the "application.properties" file.
 * <p>
 * Usage:
 * class Person {
 * {@link Value}
 * private int age;
 * }
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Value {
    /**
     * Needed to find a property by name.
     * If the value is empty try to find the property by class field name.
     *
     * @return name property to inject.
     */
    String value() default "";
}

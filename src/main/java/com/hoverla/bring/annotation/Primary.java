package com.hoverla.bring.annotation;

import com.hoverla.bring.context.ApplicationContext;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation which is used to mark class with will register in {@link ApplicationContext}
 *
 * This annotation should use only if two classes are created using the same interface.
 * <p>
 * This annotation means that from two implementations of the same interface if injecting by the interface will inject
 * bean with "Primary" annotation.
 * <p>
 * Usage:
 * interface Person{}
 * <p>
 * {@link Primary}
 * class Student implements Person{}
 * class Tutor implements Person{}
 * <p>
 * @see Bean
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Primary {
}
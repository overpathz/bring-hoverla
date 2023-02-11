package com.hoverla.bring.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation which is used to mark class with can create {@link Bean} and manually configure it.
 * <p>
 * Usage:
 * {@link Configuration}
 * class Person {
 * {@link Bean}
 * public SportService getSportService() {return new SportService()}
 * }
 * <p>
 * @see Bean
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Configuration {
}

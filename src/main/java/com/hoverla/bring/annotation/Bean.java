package com.hoverla.bring.annotation;

import com.hoverla.bring.context.ApplicationContext;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation is used to mark classes needed to create in {@link ApplicationContext}
 * After creation in {@link ApplicationContext} classes start maneging by Bring.
 * <p>
 * Usage:
 * {@link Bean}
 * class Person {}
 * <p>
 *
 * @see Configuration
 * @see Primary
 * @see Autowired
 */

@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Bean {
    /**
     * If this value is an empty name bean its class name starts in lower case.
     *
     * @return bean name
     */
    String value() default "";
}
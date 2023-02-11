package com.hoverla.bring.annotation;

import com.hoverla.bring.context.ApplicationContext;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation which is used to mark classes witch needed to inject into
 * classes mark with {@link Bean}
 * <p>
 * {@link ApplicationContext} supports three type injections:
 * - Field injection:
 * *
 * Usage:
 *  {@link Bean}
 *  public class Person {
 *      {@link Autowired}
 *      private final SportService sportService;
 *  }
 * <p>
 * - Constructor injection:
 * <p>
 *  {@link Bean}
 *  public class Person {
 *      private final SportService sportService;
 * *
 *      {@link Autowired}
 *      public Person(SportService sportService) {
 *      this.sportService = sportService
 *  }
 * <p>
 * - Setter injection
 *   public class Person {
 *       private final SportService sportService;
 *       {@link Autowired}
 *       public setPerson(SportService sportService) {
 *       this.sportService = sportService
 *   }
 *
 *   @see Bean
 */
@Retention(RUNTIME)
@Target({FIELD, METHOD, CONSTRUCTOR})
public @interface Autowired {
}